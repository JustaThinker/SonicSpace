package com.music.sonic.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

val Primary = Color(0xFF1565C0)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF0D47A1)
val OnPrimaryContainer = Color(0xFFD0E4FF)

val Secondary = Color(0xFF1976D2)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF0A3670)
val OnSecondaryContainer = Color(0xFFD0E4FF)

val Tertiary = Color(0xFF00838F)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF004D40)
val OnTertiaryContainer = Color(0xFFC2F0E8)

val Error = Color(0xFFF2B8B5)
val OnError = Color(0xFF601410)
val ErrorContainer = Color(0xFF8C1D18)
val OnErrorContainer = Color(0xFFF9DEDC)

val Outline = Color(0xFF938F99)
val OutlineVariant = Color(0xFF44474E)

val Surface = Color(0xFF141318)
val OnSurface = Color(0xFFE6E1E5)
val SurfaceVariant = Color(0xFF3F3B46)
val OnSurfaceVariant = Color(0xFFCAC4D0)

val SurfaceContainerLow = Color(0xFF17151B)
val SurfaceContainer = Color(0xFF1C1A22)
val SurfaceContainerHigh = Color(0xFF24222A)
val SurfaceContainerHighest = Color(0xFF2F2C35)

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
