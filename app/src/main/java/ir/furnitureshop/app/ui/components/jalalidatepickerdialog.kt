package ir.furnitureshop.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.furnitureshop.app.util.JalaliDate

/**
 * انتخاب تاریخ شمسی (جلالی) — سال / ماه / روز با منوهای کشویی.
 * جایگزین DatePicker میلادی Material3؛ بدون وابستگی خارجی.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalaliDatePickerDialog(
    initialDateMillis: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initial = JalaliDate.fromEpochMillis(initialDateMillis ?: System.currentTimeMillis())
    var year by remember { mutableIntStateOf(initial.first.coerceIn(JalaliDate.MIN_YEAR, JalaliDate.MAX_YEAR)) }
    var month by remember { mutableIntStateOf(initial.second.coerceIn(1, 12)) }
    var day by remember { mutableIntStateOf(initial.third) }
    val daysInMonth = JalaliDate.daysInMonth(year, month)
    val effectiveDay = day.coerceIn(1, daysInMonth)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب تاریخ شمسی", style = MaterialTheme.typography.titleMedium) },
        text = {
            Row(modifier = Modifier.fillMaxWidth()) {
                JalaliDropdown(
                    label = "سال",
                    value = "${JalaliDate.fa(year)}",
                    options = (JalaliDate.MIN_YEAR..JalaliDate.MAX_YEAR).map { JalaliDate.fa(it) },
                    onSelect = { index -> year = JalaliDate.MIN_YEAR + index },
                    modifier = Modifier.weight(1.2f)
                )
                Spacer(Modifier.width(8.dp))
                JalaliDropdown(
                    label = "ماه",
                    value = JalaliDate.MONTH_NAMES[month - 1],
                    options = JalaliDate.MONTH_NAMES,
                    onSelect = { index -> month = index + 1 },
                    modifier = Modifier.weight(1.4f)
                )
                Spacer(Modifier.width(8.dp))
                JalaliDropdown(
                    label = "روز",
                    value = JalaliDate.fa(effectiveDay),
                    options = (1..daysInMonth).map { JalaliDate.fa(it) },
                    onSelect = { index -> day = index + 1 },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(JalaliDate.toEpochMillis(year, month, effectiveDay)) }) {
                Text("تأیید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JalaliDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .padding(0.dp),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
