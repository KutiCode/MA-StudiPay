package de.throsenheim.oektem.masterarbeit.ma_studipay.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.BankDao

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.UserDao

import de.throsenheim.oektem.masterarbeit.ma_studipay.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.BankSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.User

@Database(
    entities = [User::class, Bank::class, BankSecrets::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bankDao(): BankDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
    }

