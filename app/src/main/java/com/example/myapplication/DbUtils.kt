package com.example.myapplication

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity
data class User(
    @PrimaryKey val username: String
)

@Dao
interface UserDao {
    @Query("SELECT USERNAME from User")
    suspend fun getUsername(): List<String>

    @Insert
    suspend fun insertUser(user: User)
}

@Database(entities = [User::class], version = 7)
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