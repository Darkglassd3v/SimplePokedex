package com.example.simplepokedex.ui.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.simplepokedex.data.remote.dto.PokemonResult
import com.example.simplepokedex.data.repository.PokemonRepository
import com.example.simplepokedex.ui.pokemonlist.PokemonState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.uniflow.android.AndroidDataFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
     val repository: PokemonRepository
) : AndroidDataFlow() {

    private val fullList = mutableListOf<PokemonResult>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadPokemon(offset: Int) = action {
        val currentState = getState()

        if (currentState is PokemonState.PokemonLoaded && currentState.isPaging) return@action

        if (offset == 0) {
            setState(PokemonState.Loading)
        } else if (currentState is PokemonState.PokemonLoaded) {
            setState(currentState.copy(isPaging = true))
        }

        try {
            val newList = repository.fetchPokemon(offset)
            if (offset == 0) fullList.clear()

            val uniqueItems = newList.filter { newItem ->
                fullList.none { it.name == newItem.name }
            }
            if (offset > 0 && uniqueItems.isEmpty()) {
                if (currentState is PokemonState.PokemonLoaded) {
                    setState(currentState.copy(isPaging = false))
                }
                return@action
            }

            fullList.addAll(uniqueItems)

            setState(PokemonState.PokemonLoaded(
                list = fullList.toList(),
                currentOffset = offset,
                isPaging = false
            ) )
        } catch (e: Exception) {
            println(Log.getStackTraceString(e))
        }
    }

    fun search(query: String) = action {
        val cleanedQuery = query.lowercase().trim()
        if(cleanedQuery.isBlank()){
            fullList.clear()
            loadPokemon(0)
        }else {
            val cleanedQuery = query.lowercase().trim()

            setState(PokemonState.Loading)
            if (cleanedQuery.isBlank()) {
                setState(PokemonState.PokemonLoaded(fullList.toList(), 0))
                return@action
            }
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