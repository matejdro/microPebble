package com.matejdro.micropebble.watchsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.home.ui.R
import com.matejdro.micropebble.navigation.keys.WatchSettingsScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import io.rebble.libpebblecommon.database.entity.RgbColorPreset
import io.rebble.libpebblecommon.database.entity.RgbColorWatchPref
import io.rebble.libpebblecommon.notification.DefaultVibePattern
import io.rebble.libpebblecommon.notification.VibePattern
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
@ContributesScreenBinding
class WatchSettingsScreen(
   private val viewModel: WatchSettingsViewModel,
) : Screen<WatchSettingsScreenKey>() {

   @Composable
   override fun Content(key: WatchSettingsScreenKey) {
      val state = viewModel.state.collectAsStateWithLifecycleAndBlinkingPrevention()

      WatchSettingsContent(
         state = state::value,
         onColorSelected = viewModel::setBacklightColor,
         onNotificationVibePatternSelected = viewModel::setOverrideNotificationVibePattern,
         onCalendarVibePatternSelected = viewModel::setOverrideCalendarVibePattern,
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchSettingsContent(
   state: () -> Outcome<WatchSettingsState>?,
   onColorSelected: (UInt) -> Unit,
   onNotificationVibePatternSelected: (String?) -> Unit,
   onCalendarVibePatternSelected: (String?) -> Unit,
) {
   Scaffold(
      topBar = { TopAppBar(title = { Text(stringResource(R.string.watch_settings)) }) },
   ) { innerPadding ->
      ProgressErrorSuccessScaffold(state, Modifier.padding(innerPadding)) { watchSettingsState ->
         Column(
            modifier = Modifier
               .fillMaxSize()
               .padding(innerPadding)
               .imePadding()
               .verticalScroll(rememberScrollState())
               .padding(horizontal = 16.dp),
         ) {
            Text(
               text = RgbColorWatchPref.BacklightColor.displayName,
               style = MaterialTheme.typography.titleMedium,
            )
            RgbColorWatchPref.BacklightColor.description?.let { description ->
               Text(
                  text = description,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
               )
            }

            Spacer(Modifier.height(12.dp))

            ColorPresetGrid(
               presets = BACKLIGHT_COLOR_PRESETS,
               selectedRgb = watchSettingsState.backlightColor,
               onPresetSelected = { onColorSelected(it.rgb) },
            )

            Spacer(Modifier.height(16.dp))

            CustomRgbPicker(
               currentRgb = watchSettingsState.backlightColor,
               onColorChanged = onColorSelected,
            )

            Spacer(Modifier.height(24.dp))

            VibePatternPicker(
               title = stringResource(R.string.notification_vibe_title),
               description = stringResource(R.string.notification_vibe_description),
               customVibePatterns = watchSettingsState.customVibePatterns,
               selectedPatternName = watchSettingsState.overrideNotificationVibePattern,
               onPatternSelected = onNotificationVibePatternSelected,
            )

            Spacer(Modifier.height(16.dp))

            VibePatternPicker(
               title = stringResource(R.string.calendar_vibe_title),
               description = stringResource(R.string.calendar_vibe_description),
               customVibePatterns = watchSettingsState.customVibePatterns,
               selectedPatternName = watchSettingsState.overrideCalendarVibePattern,
               onPatternSelected = onCalendarVibePatternSelected,
            )
         }
      }
   }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPresetGrid(
   presets: List<RgbColorPreset>,
   selectedRgb: UInt,
   onPresetSelected: (RgbColorPreset) -> Unit,
) {
   FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth(),
   ) {
      presets.forEach { preset ->
         val isSelected = preset.rgb == selectedRgb
         val color = preset.rgb.toComposeColor()
         Box(
            modifier = Modifier
               .size(48.dp)
               .clip(CircleShape)
               .background(color)
               .then(
                  if (isSelected) {
                     Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                  } else {
                     Modifier
                  }
               )
               .semantics { contentDescription = preset.displayName }
               .clickable { onPresetSelected(preset) },
         )
      }
   }
}

@Composable
private fun CustomRgbPicker(
   currentRgb: UInt,
   onColorChanged: (UInt) -> Unit,
) {
   var r by remember(currentRgb) { mutableFloatStateOf(((currentRgb shr RED_SHIFT) and COLOR_CHANNEL_MASK).toFloat()) }
   var g by remember(currentRgb) { mutableFloatStateOf(((currentRgb shr GREEN_SHIFT) and COLOR_CHANNEL_MASK).toFloat()) }
   var b by remember(currentRgb) { mutableFloatStateOf((currentRgb and COLOR_CHANNEL_MASK).toFloat()) }
   val currentHex = rgbToHex(r = r.toInt(), g = g.toInt(), b = b.toInt())
   var hexText by remember(currentHex) { mutableStateOf(currentHex) }

   fun emitCurrent() {
      val rgb = (r.toUInt() shl RED_SHIFT) or (g.toUInt() shl GREEN_SHIFT) or b.toUInt()
      onColorChanged(rgb)
   }

   fun onHexInput(input: String) {
      val filtered = input.uppercase().filter { it in "0123456789ABCDEF" }.take(HEX_COLOR_LENGTH)
      hexText = filtered
      if (filtered.length < HEX_COLOR_LENGTH) return
      val rgb = filtered.toUIntOrNull(HEX_RADIX) ?: return
      r = ((rgb shr RED_SHIFT) and COLOR_CHANNEL_MASK).toFloat()
      g = ((rgb shr GREEN_SHIFT) and COLOR_CHANNEL_MASK).toFloat()
      b = (rgb and COLOR_CHANNEL_MASK).toFloat()
      onColorChanged(rgb)
   }

   Column {
      Text(stringResource(R.string.backlight_color_custom), style = MaterialTheme.typography.titleSmall)

      Spacer(Modifier.height(4.dp))

      Box(
         modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(red = r / MAX_COLOR_CHANNEL, green = g / MAX_COLOR_CHANNEL, blue = b / MAX_COLOR_CHANNEL)),
      )

      Spacer(Modifier.height(8.dp))

      OutlinedTextField(
         value = hexText,
         onValueChange = ::onHexInput,
         label = { Text(stringResource(R.string.backlight_color_hex_label)) },
         prefix = { Text("#") },
         singleLine = true,
         keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done,
         ),
         modifier = Modifier.fillMaxWidth(),
      )

      Spacer(Modifier.height(8.dp))

      ColorChannelSlider("R", r, { r = it }, ::emitCurrent)
      ColorChannelSlider("G", g, { g = it }, ::emitCurrent)
      ColorChannelSlider("B", b, { b = it }, ::emitCurrent)
   }
}

@Composable
private fun ColorChannelSlider(
   label: String,
   value: Float,
   onValueChange: (Float) -> Unit,
   onValueChangeFinished: () -> Unit,
) {
   Row(verticalAlignment = Alignment.CenterVertically) {
      Text(label, modifier = Modifier.padding(end = 8.dp))
      Slider(
         value = value,
         onValueChange = onValueChange,
         onValueChangeFinished = onValueChangeFinished,
         valueRange = 0f..MAX_COLOR_CHANNEL,
         modifier = Modifier.weight(1f),
      )
      Text(text = "${value.toInt()}", modifier = Modifier.padding(start = 8.dp).size(width = 32.dp, height = 24.dp))
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VibePatternPicker(
   title: String,
   description: String,
   customVibePatterns: List<VibePattern>,
   selectedPatternName: String?,
   onPatternSelected: (String?) -> Unit,
) {
   var expanded by remember { mutableStateOf(false) }
   val noneLabel = stringResource(R.string.vibe_pattern_none)
   val defaultLabels = defaultVibePatternLabels()
   val selectedLabel = if (selectedPatternName == null) {
      noneLabel
   } else {
      defaultLabels[selectedPatternName] ?: selectedPatternName
   }

   Column {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Text(
         text = description,
         style = MaterialTheme.typography.bodySmall,
         color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.height(8.dp))
      ExposedDropdownMenuBox(
         expanded = expanded,
         onExpandedChange = { expanded = it },
      ) {
         OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
               .fillMaxWidth()
               .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
         )
         ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
         ) {
            DropdownMenuItem(
               text = { Text(text = noneLabel) },
               onClick = {
                  onPatternSelected(null)
                  expanded = false
               },
            )
            defaultLabels.forEach { (storageKey, label) ->
               DropdownMenuItem(
                  text = { Text(text = label) },
                  onClick = {
                     onPatternSelected(storageKey)
                     expanded = false
                  },
               )
            }
            customVibePatterns.forEach { pattern ->
               DropdownMenuItem(
                  text = { Text(text = pattern.name) },
                  onClick = {
                     onPatternSelected(pattern.name)
                     expanded = false
                  },
               )
            }
         }
      }
   }
}

@Composable
private fun defaultVibePatternLabels(): Map<String, String> = mapOf(
   DefaultVibePattern.Standard.displayName to stringResource(R.string.vibe_pattern_standard),
   DefaultVibePattern.Pulses.displayName to stringResource(R.string.vibe_pattern_pulses),
   DefaultVibePattern.Double.displayName to stringResource(R.string.vibe_pattern_double),
   DefaultVibePattern.Triple.displayName to stringResource(R.string.vibe_pattern_triple),
   DefaultVibePattern.Bloom.displayName to stringResource(R.string.vibe_pattern_bloom),
   DefaultVibePattern.Pips.displayName to stringResource(R.string.vibe_pattern_pips),
   DefaultVibePattern.Ole.displayName to stringResource(R.string.vibe_pattern_ole),
   DefaultVibePattern.SOS.displayName to stringResource(R.string.vibe_pattern_sos),
   DefaultVibePattern.OhhhOh.displayName to stringResource(R.string.vibe_pattern_ohhh_oh),
   DefaultVibePattern.Five.displayName to stringResource(R.string.vibe_pattern_five),
   DefaultVibePattern.Two.displayName to stringResource(R.string.vibe_pattern_two),
)

private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val COLOR_CHANNEL_MASK = 0xFFu
private const val MAX_COLOR_CHANNEL = 255f
private const val HEX_COLOR_LENGTH = 6
private const val HEX_RADIX = 16

private val BACKLIGHT_COLOR_PRESETS = listOf(
   RgbColorPreset(0x00FF0000u, "Red"),
   RgbColorPreset(0x00FF7F00u, "Orange"),
   RgbColorPreset(0x00FFFF00u, "Yellow"),
   RgbColorPreset(0x007FFF00u, "Lime"),
   RgbColorPreset(0x0000FF00u, "Green"),
   RgbColorPreset(0x0000FFFFu, "Cyan"),
   RgbColorPreset(0x000000FFu, "Blue"),
   RgbColorPreset(0x007F00FFu, "Purple"),
   RgbColorPreset(0x00FF00FFu, "Magenta"),
   RgbColorPreset(0x00FF66CCu, "Pink"),
   RgbColorPreset(0x00F0D0B0u, "Warm White"),
   RgbColorPreset(0x00FFFFFFu, "Cool White"),
)

private fun rgbToHex(r: Int, g: Int, b: Int) =
   String.format(java.util.Locale.ROOT, "%06X", (r shl RED_SHIFT) or (g shl GREEN_SHIFT) or b)

private fun UInt.toComposeColor(): Color {
   val r = ((this shr RED_SHIFT) and COLOR_CHANNEL_MASK).toFloat() / MAX_COLOR_CHANNEL
   val g = ((this shr GREEN_SHIFT) and COLOR_CHANNEL_MASK).toFloat() / MAX_COLOR_CHANNEL
   val b = (this and COLOR_CHANNEL_MASK).toFloat() / MAX_COLOR_CHANNEL
   return Color(red = r, green = g, blue = b)
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun WatchSettingsScreenPreview() {
   var color by remember { mutableStateOf(RgbColorWatchPref.BacklightColor.defaultValue) }
   PreviewTheme {
      WatchSettingsContent(
         state = {
            Outcome.Success(
               WatchSettingsState(
                  backlightColor = color,
                  customVibePatterns = listOf(VibePattern("My Custom Pattern", listOf(200, 100, 400), bundled = false)),
               )
            )
         },
         onColorSelected = { color = it },
         onNotificationVibePatternSelected = {},
         onCalendarVibePatternSelected = {},
      )
   }
}
