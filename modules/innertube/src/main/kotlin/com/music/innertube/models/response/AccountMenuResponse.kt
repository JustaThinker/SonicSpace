package com.music.innertube.models.response

import com.music.innertube.models.AccountInfo
import com.music.innertube.models.Runs
import com.music.innertube.models.Thumbnails
import kotlinx.serialization.Serializable

@Serializable
data class AccountMenuResponse(
  val actions: List<Action> = emptyList(),
) {
  @Serializable
  data class Action(
    val openPopupAction: OpenPopupAction? = null,
  ) {
    @Serializable
    data class OpenPopupAction(
      val popup: Popup? = null,
    ) {
      @Serializable
      data class Popup(
        val multiPageMenuRenderer: MultiPageMenuRenderer? = null,
      ) {
        @Serializable
        data class MultiPageMenuRenderer(
          val header: Header? = null,
        ) {
          @Serializable
          data class Header(
            val activeAccountHeaderRenderer: ActiveAccountHeaderRenderer? = null,
          ) {
            @Serializable
            data class ActiveAccountHeaderRenderer(
              val accountName: Runs? = null,
              val email: Runs? = null,
              val channelHandle: Runs? = null,
              val accountPhoto: Thumbnails? = null,
            ) {
              fun toAccountInfo() =
                AccountInfo(
                  name = accountName?.runs?.firstOrNull()?.text.orEmpty(),
                  email = email?.runs?.firstOrNull()?.text,
                  channelHandle = channelHandle?.runs?.firstOrNull()?.text,
                  thumbnailUrl = accountPhoto?.thumbnails?.lastOrNull()?.url,
                )
            }
          }
        }
      }
    }
  }
}
