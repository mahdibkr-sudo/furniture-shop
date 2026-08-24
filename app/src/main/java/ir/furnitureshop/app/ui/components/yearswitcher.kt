package ir.furnitureshop.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.furnitureshop.app.util.JalaliDate
import ir.furnitureshop.app.util.YearFilter

/**
 * دکمه سوییچ سال مالی — در هدر صفحه‌ها قرار می‌گیرد.
 * با انتخاب سال، همه آمار و لیست‌های مالی برنامه به همان سال فیلتر می‌شوند.
 */
@Composable
fun YearSwitcher(modifier: Modifier = Modifier) {
    val selectedYear by YearFilter.selectedYear.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    TextButton(onClick = { expanded = true }, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = JalaliDate.fa(selectedYear),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "انتخاب سال",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        // از جدیدترین سال به قدیمی‌ها
        (JalaliDate.MAX_YEAR downTo JalaliDate.MIN_YEAR).forEach { year ->
            DropdownMenuItem(
                text = { Text(JalaliDate.fa(year)) },
                onClick = {
                    expanded = false
                    YearFilter.switchYear(year)
                }
            )
        }
    }
}
