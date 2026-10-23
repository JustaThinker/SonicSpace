@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.music.sonic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.music.sonic.R
import com.music.sonic.constants.AppFontKey
import com.music.sonic.constants.BitChordPaletteKey
import com.music.sonic.constants.DarkModeKey
import com.music.sonic.constants.DynamicThemeKey
import com.music.sonic.constants.LiquidGlassGlobalEnabledKey
import com.music.sonic.constants.PureBlackKey
import com.music.sonic.constants.PureBlackMiniPlayerKey
import com.music.sonic.constants.SelectedThemeColorKey
import com.music.sonic.constants.UseFloatingNavBarKey
import com.music.sonic.ui.component.ThemeOption
import com.music.sonic.ui.component.getActiveThemeOption
import com.music.sonic.ui.theme.DefaultThemeColor
import com.music.sonic.utils.rememberEnumPreference
import com.music.sonic.utils.rememberPreference

@Composable
fun ThemeScreen(navController: NavController, highlightKey: String? = null) {
  val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, DarkMode.AUTO)
  val (pureBlack, onPureBlackChangeRaw) = rememberPreference(PureBlackKey, defaultValue = false)
  val (_, onPureBlackMiniPlayerChange) =
    rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)
  val onPureBlackChange: (Boolean) -> Unit = { enabled ->
    onPureBlackChangeRaw(enabled)
    onPureBlackMiniPlayerChange(enabled)
  }
  val (selectedThemeColorInt, onSelectedThemeColorChange) =
    rememberPreference(SelectedThemeColorKey, DefaultThemeColor.toArgb())
  val (dynamicTheme, onDynamicThemeChange) =
    rememberPreference(DynamicThemeKey, defaultValue = true)
  val (appFont, onAppFontChange) =
    rememberPreference(AppFontKey, defaultValue = "GOOGLE_SANS")
  val (liquidGlassEnabled, onLiquidGlassEnabledChange) =
    rememberPreference(LiquidGlassGlobalEnabledKey, defaultValue = false)
  val (bitChordPaletteEnabled, onBitChordPaletteChange) =
    rememberPreference(BitChordPaletteKey, defaultValue = false)
  val (_, onUseFloatingNavBarChange) =
    rememberPreference(UseFloatingNavBarKey, defaultValue = false)

  val currentThemeOption =
    getActiveThemeOption(dynamicTheme, bitChordPaletteEnabled, liquidGlassEnabled)
  val selectedThemeColor = Color(selectedThemeColorInt)

  val handleThemeOptionSelected: (ThemeOption) -> Unit = { option ->
    when (option) {
      ThemeOption.STATIC -> {
        onDynamicThemeChange(false)
        onBitChordPaletteChange(false)
        if (liquidGlassEnabled) onLiquidGlassEnabledChange(false)
      }
      ThemeOption.DYNAMIC -> {
        onDynamicThemeChange(true)
        onBitChordPaletteChange(false)
        if (liquidGlassEnabled) onLiquidGlassEnabledChange(false)
        onSelectedThemeColorChange(DefaultThemeColor.toArgb())
      }
      ThemeOption.BITCHORD -> {
        onBitChordPaletteChange(true)
        onDynamicThemeChange(false)
        onUseFloatingNavBarChange(true)
      }
    }
  }

  val handleColorSelection: (Color) -> Unit = { color ->
    onSelectedThemeColorChange(color.toArgb())
    onDynamicThemeChange(false)
    onBitChordPaletteChange(false)
    if (liquidGlassEnabled) onLiquidGlassEnabledChange(false)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.theme_colors), fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          androidx.compose.material3.IconButton(onClick = { navController.navigateUp() }) {
            androidx.compose.material3.Icon(
              painterResource(R.drawable.arrow_back),
              contentDescription = stringResource(R.string.cd_back)
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
          )
      )
    }
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 20.dp)
    ) {
      androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
      ) {
        item {
          ThemeSection(title = stringResource(R.string.theme_mode)) {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .background(MaterialTheme.colorScheme.surfaceContainerLow)
                  .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              ThemeChoice(
                title = "System",
                selected = darkMode == DarkMode.AUTO,
                modifier = Modifier.weight(1f),
                onClick = {
                  onDarkModeChange(DarkMode.AUTO)
                  onPureBlackChange(false)
                }
              )
              ThemeChoice(
                title = "Light",
                selected = darkMode == DarkMode.OFF,
                modifier = Modifier.weight(1f),
                onClick = {
                  onDarkModeChange(DarkMode.OFF)
                  onPureBlackChange(false)
                }
              )
              ThemeChoice(
                title = "Dark",
                selected = darkMode == DarkMode.ON && !pureBlack,
                modifier = Modifier.weight(1f),
                onClick = {
                  onDarkModeChange(DarkMode.ON)
                  onPureBlackChange(false)
                }
              )
              ThemeChoice(
                title = "AMOLED",
                selected = pureBlack,
                modifier = Modifier.weight(1f),
                onClick = {
                  onDarkModeChange(DarkMode.ON)
                  onPureBlackChange(true)
                }
              )
            }
          }
        }

        item {
          ThemeSection(
            title = stringResource(R.string.theme_style),
            description = currentThemeOption.description
          ) {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .background(MaterialTheme.colorScheme.surfaceContainerLow)
                  .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              ThemeOption.entries.forEach { option ->
                ThemeChoice(
                  title = option.title,
                  selected = currentThemeOption == option,
                  modifier = Modifier.weight(1f),
                  onClick = { handleThemeOptionSelected(option) }
                )
              }
            }

            AnimatedVisibility(visible = currentThemeOption == ThemeOption.STATIC) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Accent color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                  )
                  if (selectedThemeColor.toArgb() != DefaultThemeColor.toArgb()) {
                    Text(
                      text = "Reset",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.clickable { handleColorSelection(DefaultThemeColor) }
                    )
                  }
                }

                val presets =
                  listOf(
                    "Default" to DefaultThemeColor,
                    "Ocean" to Color(0xFF0288D1),
                    "Purple" to Color(0xFF7E57C2),
                    "Emerald" to Color(0xFF00897B),
                    "Sunset" to Color(0xFFFB8C00),
                    "Rose" to Color(0xFFE91E63),
                    "Indigo" to Color(0xFF3F51B5)
                  )
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(18.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  presets.forEach { (name, color) ->
                    val isSelected = selectedThemeColor.toArgb() == color.toArgb()
                    Box(
                      modifier =
                        Modifier.size(32.dp)
                          .clip(CircleShape)
                          .background(color)
                          .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                          )
                          .padding(if (isSelected) 3.dp else 0.dp)
                          .clip(CircleShape)
                          .semantics {
                            contentDescription = name
                            selected = isSelected
                          }
                          .clickable { handleColorSelection(color) }
                    )
                  }
                }
                HsvColorPicker(
                  initialColor = selectedThemeColor,
                  onColorCommit = handleColorSelection
                )
              }
            }
          }
        }

        item {
          ThemeSection(title = stringResource(R.string.liquid_glass)) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(
                modifier =
                  Modifier.weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { navController.navigate("settings/appearance/liquidglass") }
                    .padding(vertical = 8.dp)
              ) {
                Text(
                  text = "Liquid Glass",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Adjust blur and translucency",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Switch(
                checked = liquidGlassEnabled,
                onCheckedChange = onLiquidGlassEnabledChange
              )
            }
          }
        }

        item {
          ThemeSection(title = "App font") {
            val fontOptions =
              listOf(
                "GOOGLE_SANS" to "Google Sans",
                "SYSTEM" to "System default",
                "MONOSPACE" to "Monospace",
                "SANS_FLEX" to "Sans Flex"
              )
            Column {
              fontOptions.forEachIndexed { index, (key, label) ->
                val isSelected =
                  appFont.equals(key, ignoreCase = true) ||
                    (key == "GOOGLE_SANS" && appFont.equals("DEFAULT", ignoreCase = true))
                Row(
                  modifier =
                    Modifier.fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onAppFontChange(key) }
                      )
                      .padding(vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface
                  )
                  if (isSelected) {
                    Box(
                      modifier =
                        Modifier.size(8.dp)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primary)
                    )
                  }
                }
                if (index < fontOptions.lastIndex) {
                  HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ThemeSection(
  title: String,
  description: String? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerLow)
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      content = content
    )
    description?.let {
      Text(
        text = it,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 2.dp)
      )
    }
  }
}

@Composable
private fun ThemeChoice(
  title: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(
          if (selected) MaterialTheme.colorScheme.secondaryContainer
          else Color.Transparent
        )
        .selectable(
          selected = selected,
          role = Role.RadioButton,
          onClick = onClick
        )
        .padding(horizontal = 4.dp, vertical = 11.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
      color =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1
    )
  }
}

@Composable
fun HsvColorPicker(
  modifier: Modifier = Modifier,
  initialColor: Color,
  onColorCommit: (Color) -> Unit
) {
  var hsv by remember {
    val values = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), values)
    mutableStateOf(values)
  }

  LaunchedEffect(initialColor) {
    val currentColor = android.graphics.Color.HSVToColor(hsv)
    if (currentColor != initialColor.toArgb()) {
      val values = FloatArray(3)
      android.graphics.Color.colorToHSV(initialColor.toArgb(), values)
      hsv = values
    }
  }

  var hue by remember { mutableFloatStateOf(hsv[0]) }
  var saturation by remember { mutableFloatStateOf(hsv[1]) }
  var value by remember { mutableFloatStateOf(hsv[2]) }

  LaunchedEffect(hsv) {
    hue = hsv[0]
    saturation = hsv[1]
    value = hsv[2]
  }

  fun currentColor(): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))

  fun commitColor() {
    hsv = floatArrayOf(hue, saturation, value)
    onColorCommit(currentColor())
  }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(currentColor())
    )
    CustomColorSlider(
      value = hue,
      onValueChange = { hue = it },
      onValueChangeFinished = { commitColor() },
      valueRange = 0f..360f,
      label = "Custom color",
      brush =
        Brush.horizontalGradient(
          colors =
            listOf(
              Color.Red,
              Color.Yellow,
              Color.Green,
              Color.Cyan,
              Color.Blue,
              Color.Magenta,
              Color.Red
            )
        )
    )
  }
}

@Composable
private fun CustomColorSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  onValueChangeFinished: () -> Unit,
  valueRange: ClosedFloatingPointRange<Float>,
  label: String,
  brush: Brush
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 4.dp)
    )
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(28.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(brush),
      contentAlignment = Alignment.CenterStart
    ) {
      Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        colors =
          SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent
          ),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
