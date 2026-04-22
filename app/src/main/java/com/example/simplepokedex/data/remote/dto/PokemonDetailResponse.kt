package com.example.simplepokedex.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonDetailResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "sprites") val sprites: Sprites,
    @Json(name = "types") val types: List<TypeEntry>
)

@JsonClass(generateAdapter = true)
data class TypeEntry(
    @Json(name = "slot") val slot: Int,
    @Json(name = "type") val type: TypeInfo
)

@JsonClass(generateAdapter = true)
data class TypeInfo(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class Sprites(
    @Json(name = "front_default") val frontDefault: String?
)

@JsonClass(generateAdapter = true)
data class TypeResponse(
    @Json(name = "pokemon") val pokemonList: List<TypePokemonEntry>
)

@JsonClass(generateAdapter = true)
data class TypePokemonEntry(
    @Json(name = "pokemon") val pokemon: PokemonResult
)

@JsonClass(generateAdapter = true)
data class PokemonSpeciesResponse(
    @Json(name = "flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>
)

@JsonClass(generateAdapter = true)
data class FlavorTextEntry(
    @Json(name = "flavor_text") val flavorText: String,
    @Json(name = "language") val language: NamedResource
)

@JsonClass(generateAdapter = true)
data class NamedResource(
    @Json(name = "name") val name: String
)