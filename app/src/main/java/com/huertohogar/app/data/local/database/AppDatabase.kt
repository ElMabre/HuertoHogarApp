package com.huertohogar.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.huertohogar.app.data.local.dao.CartDao
import com.huertohogar.app.data.local.entity.CartEntity

@Database(entities = [CartEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Exponemos los DAOs
    abstract fun cartDao(): CartDao

    companion object {
        // Volatile asegura que todos los hilos vean la versión más reciente de la instancia
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huertohogar_database"
                )
                    // .fallbackToDestructiveMigration() // Descomentar si cambia la BD y no hay que migrar datos
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}