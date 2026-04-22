package com.example.simplepokedex.data.remote.dto

data class PokemonResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonResult>
)

data class PokemonResult(
    val name: String,
    val url: String = ""
) {
    val id: Int get() = url.trimEnd('/').split('/').lastOrNull()?.toIntOrNull() ?: 0

    fun getImageUrl(): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }
}