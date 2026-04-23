package com.example.simplepokedex.data.repository

import com.example.simplepokedex.data.local.dao.FavoriteDao
import com.example.simplepokedex.data.local.entities.FavoriteEntity
import com.example.simplepokedex.data.remote.PokeApiService
import com.example.simplepokedex.data.remote.dto.PokemonDetailResponse
import com.example.simplepokedex.data.remote.dto.PokemonResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val favoriteDao: FavoriteDao
) {
    fun getFavoriteIds() = favoriteDao.getAllFavoriteIds()

    suspend fun fetchPokemon(offset: Int): List<PokemonResult> {
        return try {
            val response = apiService.getPokemonList(offset = offset)
            response.results
        } catch (e: Exception) {
            throw e
        }
    }


    suspend fun getPokemonByName(name: String): PokemonDetailResponse {
        return apiService.getPokemonByName(name)
    }


    suspend fun getPokemonByType(typeName: String): List<PokemonResult> {
        return try {
            val response = apiService.getPokemonByType(typeName)
            response.pokemonList.map { it.pokemon }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPokemonDescription(id: Int): String {
        return try {
            val species = apiService.getPokemonSpecies(id)
            val entry = species.flavorTextEntries.firstOrNull { it.language.name == "en" }
            entry?.flavorText?.replace("\n", " ")?.replace("\u000c", " ")
                ?: "No description available."
        } catch (e: Exception) {
            "Description not available."
        }
    }

    suspend fun toggleFavorite(id: Int) {
        if (favoriteDao.isFavorite(id)) {
            favoriteDao.deleteFavorite(FavoriteEntity(id))
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(id))
        }
    }

    suspend fun getPokemonById(id: Int) = apiService.getPokemonById(id)
}