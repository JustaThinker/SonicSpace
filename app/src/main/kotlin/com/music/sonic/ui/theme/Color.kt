package com.music.sonic.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

val Primary = Color(0xFF1E88E5)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF1565C0)
val OnPrimaryContainer = Color(0xFFE3F2FD)

val Secondary = Color(0xFF42A5F5)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF1976D2)
val OnSecondaryContainer = Color(0xFFE3F2FD)

val Tertiary = Color(0xFF00ACC1)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF00838F)
val OnTertiaryContainer = Color(0xFFE0F7FA)

val Error = Color(0xFFF2B8B5)
val OnError = Color(0xFF601410)
val ErrorContainer = Color(0xFF8C1D18)
val OnErrorContainer = Color(0xFFF9DEDC)

val Outline = Color(0xFF938F99)
val OutlineVariant = Color(0xFF44474E)

val Surface = Color(0xFF1C1B1F)
val OnSurface = Color(0xFFE6E1E5)
val SurfaceVariant = Color(0xFF49454F)
val OnSurfaceVariant = Color(0xFFCAC4D0)

val SurfaceContainerLow = Color(0xFF1D1B20)
val SurfaceContainer = Color(0xFF211F26)
val SurfaceContainerHigh = Color(0xFF2B2930)
val SurfaceContainerHighest = Color(0xFF36343B)

// Standard Android ARGB Ints for XML/Canvas/Legacy View compatibility
val PrimaryArgb: Int get() = Primary.toArgb()
val OnPrimaryArgb: Int get() = OnPrimary.toArgb()
val PrimaryContainerArgb: Int get() = PrimaryContainer.toArgb()
val OnPrimaryContainerArgb: Int get() = OnPrimaryContainer.toArgb()

val SecondaryArgb: Int get() = Secondary.toArgb()
val OnSecondaryArgb: Int get() = OnSecondary.toArgb()
val SecondaryContainerArgb: Int get() = SecondaryContainer.toArgb()
val OnSecondaryContainerArgb: Int get() = OnSecondaryContainer.toArgb()

val TertiaryArgb: Int get() = Tertiary.toArgb()

val SurfaceArgb: Int get() = Surface.toArgb()
val OnSurfaceArgb: Int get() = OnSurface.toArgb()
val SurfaceVariantArgb: Int get() = SurfaceVariant.toArgb()
val OnSurfaceVariantArgb: Int get() = OnSurfaceVariant.toArgb()

val SurfaceContainerLowArgb: Int get() = SurfaceContainerLow.toArgb()
val SurfaceContainerArgb: Int get() = SurfaceContainer.toArgb()
val SurfaceContainerHighArgb: Int get() = SurfaceContainerHigh.toArgb()
val SurfaceContainerHighestArgb: Int get() = SurfaceContainerHighest.toArgb()
