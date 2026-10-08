package com.music.sonic.eq.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * AudioProcessor supporting:
 * 1) Mid-side spatial 360° stereo widener (soundstage width 1.0x to 2.0x)
 * 2) Stereo channel balance (-1.0 full Left to +1.0 full Right)
 * 3) Headphone crossfeed blend
 */
@UnstableApi
class StereoWidenerAudioProcessor : AudioProcessor {

  /** 1.0 = unchanged, >1.0 widens soundstage. */
  @Volatile
  var width: Float = 1f
    set(value) {
      field = value.coerceIn(1f, 2f)
    }

  /** Stereo Balance: -1.0 (Full Left), 0.0 (Centered), +1.0 (Full Right) */
  @Volatile
  var balance: Float = 0f
    set(value) {
      field = value.coerceIn(-1f, 1f)
    }

  /** Headphone crossfeed blend mode */
  @Volatile
  var crossfeed: Boolean = false

  private var channelCount = 0
  private var isActive = false

  private var outputBuffer: ByteBuffer = EMPTY_BUFFER
  private var inputEnded = false

  companion object {
    private val EMPTY_BUFFER: ByteBuffer =
      ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
  }

  override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
    if (inputAudioFormat.channelCount != 1 && inputAudioFormat.channelCount != 2) {
      throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
    }

    channelCount = inputAudioFormat.channelCount
    isActive = true

    // Request 16-bit PCM output format so Media3 automatically converts incoming float/high-res PCM
    return AudioProcessor.AudioFormat(
      inputAudioFormat.sampleRate,
      inputAudioFormat.channelCount,
      C.ENCODING_PCM_16BIT
    )
  }

  override fun isActive(): Boolean = isActive

  override fun queueInput(inputBuffer: ByteBuffer) {
    val inputSize = inputBuffer.remaining()
    if (inputSize == 0) return

    if (outputBuffer.capacity() < inputSize) {
      outputBuffer = ByteBuffer.allocateDirect(inputSize).order(ByteOrder.nativeOrder())
    } else {
      outputBuffer.clear()
    }

    val widthNow = width
    val balanceNow = balance
    val crossfeedNow = crossfeed
    val sampleCount = inputSize / 2

    when (channelCount) {
      2 ->
        repeat(sampleCount / 2) {
          var left = inputBuffer.getShort().toDouble()
          var right = inputBuffer.getShort().toDouble()

          if (crossfeedNow) {
            val lCross = left * 0.85 + right * 0.15
            val rCross = right * 0.85 + left * 0.15
            left = lCross
            right = rCross
          }

          if (widthNow > 1.0f) {
            val mid = (left + right) * 0.5
            val side = (left - right) * 0.5 * widthNow
            left = mid + side
            right = mid - side
          }

          if (balanceNow < 0f) {
            right *= (1f + balanceNow)
          } else if (balanceNow > 0f) {
            left *= (1f - balanceNow)
          }

          val peak = maxOf(kotlin.math.abs(left), kotlin.math.abs(right))
          if (peak > 32767.0) {
            val scale = 32767.0 / peak
            left *= scale
            right *= scale
          }

          outputBuffer.putShort(left.toInt().toShort())
          outputBuffer.putShort(right.toInt().toShort())
        }
      // Mono has no side component to widen — pass through unchanged.
      else -> repeat(sampleCount) { outputBuffer.putShort(inputBuffer.getShort()) }
    }

    outputBuffer.flip()
  }

  override fun getOutput(): ByteBuffer {
    val buffer = outputBuffer
    outputBuffer = EMPTY_BUFFER
    return buffer
  }

  override fun isEnded(): Boolean = inputEnded && outputBuffer.remaining() == 0

  @Deprecated("Deprecated in Java")
  override fun flush() {
    outputBuffer = EMPTY_BUFFER
    inputEnded = false
  }

  override fun reset() {
    @Suppress("DEPRECATION") flush()
    channelCount = 0
    isActive = false
  }

  override fun queueEndOfStream() {
    inputEnded = true
  }
}
