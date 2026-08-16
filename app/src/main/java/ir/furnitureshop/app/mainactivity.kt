package ir.furnitureshop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.furnitureshop.app.navigation.AppNavHost
import ir.furnitureshop.app.ui.theme.FurnitureShopTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Force RTL regardless of device locale (Persian-first app).
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FurnitureShopTheme {
                    AppNavHost()
                }
            }
        }
    }
}
