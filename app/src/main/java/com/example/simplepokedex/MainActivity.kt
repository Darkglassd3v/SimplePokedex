package com.example.simplepokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.simplepokedex.ui.favourites.FavoritesScreen
import com.example.simplepokedex.ui.pokemonlist.PokemonListScreen
import com.example.simplepokedex.ui.theme.SimplePokedexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimplePokedexTheme {
                SimplePokedexApp()
            }
        }
    }
}

@Composable
fun SimplePokedexApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            contentDescription = destination.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentDestination) {
                AppDestinations.HOME -> PokemonListScreen()
                AppDestinations.FAVORITES -> FavoritesScreen(
                    onBackClick = { currentDestination = AppDestinations.HOME }
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Pokedex", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite)
}