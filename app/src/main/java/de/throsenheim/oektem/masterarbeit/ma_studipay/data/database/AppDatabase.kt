package de.throsenheim.oektem.masterarbeit.ma_studipay.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.BankDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

/**
 * AppDatabase is the main database for the application.
 *
 * It defines the entities (tables) present in the database: User, Bank, and BankSecrets.
 * The database uses a version number (version = 10) and is configured not to export its schema.
 */
@Database(
    entities = [User::class, Bank::class, BankSecrets::class],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Provide an abstract function to access UserDao.
    abstract fun userDao(): UserDao

    // Provide an abstract function to access BankDao.
    abstract fun bankDao(): BankDao

    companion object {
        // Volatile variable to ensure that INSTANCE is visible to all threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retrieves the singleton instance of AppDatabase.
         * If an instance does not exist, it synchronizes and creates a new one.
         *
         * @param context The application context.
         * @return The singleton AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance or create a new one in a thread-safe manner.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database"  // Name of the database file.
                )
                    .fallbackToDestructiveMigration() // Recreates the database on schema changes.
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
