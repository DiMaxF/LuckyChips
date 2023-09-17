package com.oleg2013.casino

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Data {

    @Entity(tableName = "options_data")
    data class OptionsFields(
        @PrimaryKey val name: String,
        @ColumnInfo(name = "value") val value: Boolean
    )

    @Dao
    interface OptionsFieldsDao {
        @Insert
        fun insertAll(vararg field: OptionsFields?)

        @Query("UPDATE options_data SET value=:value WHERE name = :field")
        fun updateFieldValue(field: String, value: Boolean)

        @Delete
        fun delete(field: OptionsFields?)

        @Query("SELECT * FROM options_data")
        fun allFields(): List<OptionsFields?>?

        @Query("SELECT * FROM options_data WHERE name LIKE :name")
        fun getFieldByName(name: String?): OptionsFields?

        @Query("SELECT EXISTS(SELECT * FROM options_data WHERE name = :name)")
        fun isExist(name : String) : Boolean
    }

    @Database(version = 1, entities = [OptionsFields::class])
    abstract class AppDatabase : RoomDatabase() {
        abstract fun getStatisticDao(): OptionsFieldsDao
    }
}