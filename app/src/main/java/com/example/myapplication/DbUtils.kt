package com.example.myapplication

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.UUID

/**
 * Stores persisted login details about the user
 * @param id: Automatically generated primary key (due to concurrent
 * queries username cannot be guaranteed unique)
 * @param username: Person's username
 * @param password: Person's password
 */
@Entity
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val password: String
)

/**
 * Format the user's data is used in by the rest of the code
 * @param username: Person's username
 * @param password: Person's password
 */
data class Credentials(
    val username: String,
    val password: String
)

/**
 *  Database access object (DAO) used to manipulate the the database
 */
@Dao
interface UserDao {
    /**
     * retrieves a list (0 or 1 items) of logged in users
     * @return User's credentials, if they exist
     */
    @Query("SELECT USERNAME, PASSWORD from User")
    suspend fun getUserPass(): List<Credentials>

    /**
     * Deletes all users from the database
     */
    @Query("DELETE FROM User")
    suspend fun deleteUser()

    /**
     * Inserts a new user into the database
     * @param user: User to insert
     */
    @Insert
    suspend fun insertUser(user: User)
}

/**
 * Room database used
 */
@Database(entities = [User::class], version = 12)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Gets the DAO for accessing users
     * @return Instance of the DAO
     */
    abstract fun UserDao(): UserDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        /**
         * Follows the singleton pattern to get an instance of the app
         * database
         * @param context: THe application context
         * @return the instance of the app database
         */
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "db"
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE!!
        }
    }
}