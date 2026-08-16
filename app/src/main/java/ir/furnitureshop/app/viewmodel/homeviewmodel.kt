package ir.furnitureshop.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val appName: String = "فروشگاه مبل و میز"
)

/**
 * ViewModel صفحه اصلی — فقط نام اپ؛ صفحه اصلی صرفاً ناوبری است و آماری ندارد.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
