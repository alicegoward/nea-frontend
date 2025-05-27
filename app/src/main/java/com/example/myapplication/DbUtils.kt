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

@Entity
data class User (
    @PrimaryKey val username: String,
    val password: String
)

data class Credentials (
    val username: String,
    val password: String
)

@Dao
interface UserDao {
    @Query("SELECT USERNAME, PASSWORD from User")
    suspend fun getUserPass(): List<Credentials>


    @Query("DELETE FROM User")
    suspend fun deleteUser()

    @Insert
    suspend fun insertUser(user: User)
}

@Database(entities = [User::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDao(): UserDao
    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "db").fallbackToDestructiveMigration().build()
            }
            return INSTANCE!!
        }
    }
}