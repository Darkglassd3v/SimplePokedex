package com.example.simplepokedex.ui.favourites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplepokedex.data.remote.dto.PokemonResult
import com.example.simplepokedex.ui.pokemonlist.PokemonCard
import com.example.simplepokedex.ui.viewmodels.PokemonViewModel
import com.example.simplepokedex.R

@Composable
fun FavoritesScreen(
    viewModel: PokemonViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val favIds by viewModel.favoriteIds.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.go_back_title))
            }
            Text(
                text = stringResource(R.string.favorites_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (favIds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorites yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    count = favIds.size,
                    key = { index -> favIds[index] }
                ) { index ->
                    val pokemonId = favIds[index]
                    val placeholderResult = remember(pokemonId) {
                        PokemonResult(
                            name = "",
                            url = "https://pokeapi.co/api/v2/pokemon/$pokemonId/"
                        )
                    }

                    PokemonCard(
                        pokemon = placeholderResult,
                        isFavorite = true,
                        onFavoriteClick = { viewModel.toggleFavorite(pokemonId) },
                        fetchDetails = { viewModel.repository.getPokemonById(pokemonId) },
                        fetchDescription = { viewModel.repository.getPokemonDescription(pokemonId) }
                    )
                }
            }
        }
    }
}