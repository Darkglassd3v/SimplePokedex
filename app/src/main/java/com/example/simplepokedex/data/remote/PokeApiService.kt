package com.example.simplepokedex.data.remote

import com.example.simplepokedex.data.remote.dto.PokemonDetailResponse
import com.example.simplepokedex.data.remote.dto.PokemonResponse
import com.example.simplepokedex.data.remote.dto.PokemonSpeciesResponse
import com.example.simplepokedex.data.remote.dto.TypeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int
    ): PokemonResponse

    @GET("pokemon/{name}")
    suspend fun getPokemonByName(
        @Path("name") name: String
    ): PokemonDetailResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonById(
        @Path("id") id: Int
    ): PokemonDetailResponse

    @GET("type/{idOrName}")
    suspend fun getPokemonByType(
        @Path("idOrName") idOrName: String
    ): TypeResponse

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesResponse
}