package com.example.simplepokedex.ui.pokemonlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplepokedex.R
import com.example.simplepokedex.com.example.simplepokedex.ui.viewmodels.PokemonState
import com.example.simplepokedex.data.remote.dto.PokemonResult
import com.example.simplepokedex.ui.viewmodels.PokemonViewModel
import io.uniflow.android.livedata.states
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PokemonListScreen(
    modifier: Modifier = Modifier,
    viewModel: PokemonViewModel = hiltViewModel()
) {
    val viewState by viewModel.states.observeAsState(initial = PokemonState.Loading)
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(searchQuery) {
        if (searchQuery.isEmpty()) {
            viewModel.search(searchQuery)
        } else if (searchQuery.length >= 3) {
            kotlinx.coroutines.delay(500)
            viewModel.search(searchQuery)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                    append(stringResource(R.string.title_1))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.title_2))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = viewState) {
                is PokemonState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is PokemonState.PokemonLoaded -> {
                    PokemonList(
                        pokemonList = state.list,
                        isPaging = state.isPaging,
                        favoriteIds = favoriteIds,
                        onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                        onLoadMore = { viewModel.loadPokemon(state.list.size) },
                        viewModel = viewModel
                    )
                }

                is PokemonState.Error -> {}
            }
        }
    }
}

@Composable
fun PokemonList(
    pokemonList: List<PokemonResult>,
    isPaging: Boolean,
    favoriteIds: List<Int>,
    onFavoriteClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    viewModel: PokemonViewModel
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, isPaging) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .debounce(300L)
            .collect { lastIndex ->
                val totalItems = pokemonList.size
                if (lastIndex != null && lastIndex >= totalItems - 5 && !isPaging && totalItems > 0) {
                    onLoadMore()
                }
            }
    }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(
            count = pokemonList.size,
            key = { index -> pokemonList[index].name }
        ) { index ->
            val pokemon = pokemonList[index]
            PokemonCard(
                pokemon = pokemon,
                isFavorite = favoriteIds.contains(pokemon.id),
                onFavoriteClick = { onFavoriteClick(pokemon.id) },
                viewModel = viewModel
            )
        }
        if (isPaging) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .size(32.dp)
                )
            }
        }
    }
}

