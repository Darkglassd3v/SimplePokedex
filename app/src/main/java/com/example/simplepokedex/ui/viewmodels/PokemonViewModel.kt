package com.example.simplepokedex.ui.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.simplepokedex.com.example.simplepokedex.ui.viewmodels.PokemonState
import com.example.simplepokedex.data.remote.dto.PokemonDetailResponse
import com.example.simplepokedex.data.remote.dto.PokemonResult
import com.example.simplepokedex.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.uniflow.android.AndroidDataFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    val repository: PokemonRepository
) : AndroidDataFlow() {

    private val fullList = mutableListOf<PokemonResult>()

    @Volatile
    private var isLoadingPage = false

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _pokemonDetails = MutableStateFlow<Map<Int, PokemonDetailResponse>>(emptyMap())
    val pokemonDetails: StateFlow<Map<Int, PokemonDetailResponse>> = _pokemonDetails.asStateFlow()

    private val _pokemonDescriptions = MutableStateFlow<Map<Int, String>>(emptyMap())
    val pokemonDescriptions: StateFlow<Map<Int, String>> = _pokemonDescriptions.asStateFlow()

    private val fetchingIds = mutableSetOf<Int>()

    fun loadCardDetails(pokemonId: Int, name: String) {
        if (_pokemonDetails.value.containsKey(pokemonId)) return
        if (!fetchingIds.add(pokemonId)) return

        viewModelScope.launch {
            try {
                val details = if (name.isNotBlank()) {
                    repository.getPokemonByName(name)
                } else {
                    repository.getPokemonById(pokemonId)
                }
                _pokemonDetails.update { it + (pokemonId to details) }

                val desc = repository.getPokemonDescription(pokemonId)
                _pokemonDescriptions.update { it + (pokemonId to desc) }
            } catch (e: Exception) {
                _pokemonDescriptions.update { it + (pokemonId to "N/A") }
            } finally {
                fetchingIds.remove(pokemonId)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadPokemon(offset: Int) = action {
        if (isLoadingPage) return@action
        isLoadingPage = true

        try {
            val currentState = getState()

            if (offset == 0) {
                setState(PokemonState.Loading)
            } else if (currentState is PokemonState.PokemonLoaded) {
                setState(currentState.copy(isPaging = true))
            }

            val newList = repository.fetchPokemon(offset)
            if (offset == 0) fullList.clear()

            val uniqueItems = newList.filter { newItem ->
                fullList.none { it.name == newItem.name }
            }

            if (offset > 0 && uniqueItems.isEmpty()) {
                val state = getState()
                if (state is PokemonState.PokemonLoaded) {
                    setState(state.copy(isPaging = false))
                }
                return@action
            }

            fullList.addAll(uniqueItems)

            setState(
                PokemonState.PokemonLoaded(
                    list = fullList.toList(),
                    currentOffset = offset,
                    isPaging = false
                )
            )
        } catch (e: Exception) {
            println(Log.getStackTraceString(e))
        } finally {
            isLoadingPage = false
        }
    }

    fun search(query: String) = action {
        val cleanedQuery = query.lowercase().trim()
        if (cleanedQuery.isBlank()) {
            fullList.clear()
            loadPokemon(0)
        } else {
            setState(PokemonState.Loading)
            try {
                val typeResults = repository.getPokemonByType(cleanedQuery)

                if (typeResults.isNotEmpty()) {
                    setState(PokemonState.PokemonLoaded(typeResults, 0))
                } else {
                    val detail = repository.getPokemonByName(cleanedQuery)
                    val searchResult = PokemonResult(
                        name = detail.name,
                        url = "https://pokeapi.co/api/v2/pokemon/${detail.id}/"
                    )
                    setState(PokemonState.PokemonLoaded(listOf(searchResult), 0))
                }
            } catch (e: Exception) {
                setState(PokemonState.PokemonLoaded(emptyList(), 0))
            }
        }
    }

    val favoriteIds: StateFlow<List<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(id: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }
}