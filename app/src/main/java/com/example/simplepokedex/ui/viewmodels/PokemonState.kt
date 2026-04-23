package com.example.simplepokedex.com.example.simplepokedex.ui.viewmodels

import com.example.simplepokedex.data.remote.dto.PokemonResult
import io.uniflow.core.flow.data.UIState

sealed class PokemonState : UIState() {
    object Loading : PokemonState()
    data class PokemonLoaded(
        val list: List<PokemonResult>,
        val currentOffset: Int,
        val isPaging: Boolean = false
    ) : PokemonState()

    data class Error(val message: String) : PokemonState()
}