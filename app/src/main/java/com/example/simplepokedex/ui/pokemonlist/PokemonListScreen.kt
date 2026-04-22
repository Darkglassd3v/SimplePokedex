package com.example.simplepokedex.ui.pokemonlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.simplepokedex.R
import com.example.simplepokedex.data.remote.dto.PokemonResult
import com.example.simplepokedex.data.remote.dto.PokemonDetailResponse
import com.example.simplepokedex.ui.viewmodels.PokemonViewModel
import io.uniflow.android.livedata.states

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
                        fetchDetails = { name -> viewModel.repository.getPokemonByName(name) },
                        fetchDescription = { id -> viewModel.repository.getPokemonDescription(id) }
                    )
                }
                is PokemonState.Error -> {
                }
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
    fetchDetails: suspend (String) -> PokemonDetailResponse,
    fetchDescription: suspend (Int) -> String
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, isPaging) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                val totalItems = pokemonList.size
                if (lastIndex != null && lastIndex >= totalItems - 1 && !isPaging && totalItems > 0) {
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
                fetchDetails = { fetchDetails(pokemon.name) },
                fetchDescription = { fetchDescription(pokemon.id) }
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

@Composable
fun PokemonCard(
    pokemon: PokemonResult,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    fetchDetails: suspend () -> PokemonDetailResponse,
    fetchDescription: suspend () -> String
) {
    var pokemonName by remember(pokemon.id) { mutableStateOf(pokemon.name) }
    var types by remember(pokemon.id) { mutableStateOf<List<String>?>(null) }
    var description by remember(pokemon.id) { mutableStateOf<String?>(null) }
    val noDescription = stringResource(id = R.string.info_not_available)

    LaunchedEffect(pokemon.id) {
        try {
            val details = fetchDetails()
            types = details.types.map { it.type.name }

            if (pokemonName.isEmpty()) {
                pokemonName = details.name
            }
            description = fetchDescription()
        } catch (e: Exception) {
            description = noDescription
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.getImageUrl(),
                contentDescription = pokemonName,
                modifier = Modifier.size(80.dp)
            )

            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)) {
                Text(
                    text = pokemonName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types?.forEach { typeName ->
                        TypeBadge(type = typeName)
                    } ?: Box(Modifier.size(40.dp, 16.dp))
                }
                Text(
                    text = description ?: stringResource(id = R.string.loading_description),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
fun TypeBadge(type: String) {
    val typeColor = when (type.lowercase()) {
        "normal" -> Color(0xFFA8A77A)
        "fire" -> Color(0xFFEE8130)
        "water" -> Color(0xFF6390F0)
        "electric" -> Color(0xFFF7D02C)
        "grass" -> Color(0xFF7AC74C)
        "ice" -> Color(0xFF96D9D6)
        "fighting" -> Color(0xFFC22E28)
        "poison" -> Color(0xFFA33EA1)
        "ground" -> Color(0xFFE2BF65)
        "flying" -> Color(0xFFA98FF3)
        "psychic" -> Color(0xFFF95587)
        "bug" -> Color(0xFFA6B91A)
        "rock" -> Color(0xFFB6A136)
        "ghost" -> Color(0xFF735797)
        "dragon" -> Color(0xFF6F35FC)
        "dark" -> Color(0xFF705746)
        "steel" -> Color(0xFFB7B7CE)
        "fairy" -> Color(0xFFD685AD)
        else -> Color.White // Bianco per il tipo default
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = typeColor,
        modifier = Modifier.padding(end = 4.dp),
        border = if (typeColor == Color.White) BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Text(
            text = type.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (typeColor == Color.White) Color.DarkGray else Color.White
        )
    }
}