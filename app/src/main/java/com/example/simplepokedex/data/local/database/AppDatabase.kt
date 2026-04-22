package com.example.simplepokedex.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.simplepokedex.data.local.dao.FavoriteDao
import com.example.simplepokedex.data.local.entities.FavoriteEntity

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}