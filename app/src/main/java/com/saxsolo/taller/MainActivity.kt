package com.saxsolo.taller

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saxsolo.taller.practice.PracticeViewModel
import com.saxsolo.taller.ui.Bg
import com.saxsolo.taller.ui.Brass
import com.saxsolo.taller.ui.Ink
import com.saxsolo.taller.ui.LabScreen
import com.saxsolo.taller.ui.Muted
import com.saxsolo.taller.ui.PhrasesScreen
import com.saxsolo.taller.ui.Surf
import com.saxsolo.taller.ui.TenorTheme
import com.saxsolo.taller.ui.TransposerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // el músico tiene las manos en el sax
        setContent { TenorTheme { App() } }
    }
}

private val TABS = listOf("Transpositor" to Icons.Filled.Refresh, "Fraseos" to Icons.Filled.List, "Laboratorio" to Icons.Filled.PlayArrow)

@Composable
fun App(vm: PracticeViewModel = viewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(2) }
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Surf) {
                TABS.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Icon(icon, contentDescription = label) }, label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink, indicatorColor = Brass, selectedTextColor = Brass,
                            unselectedIconColor = Muted, unselectedTextColor = Muted,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> TransposerScreen(vm)
                1 -> PhrasesScreen(vm)
                else -> LabScreen(vm)
            }
        }
    }
}
