package com.diskin.alon.pagoda

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `bookmarked_locations` (`lat` REAL NOT NULL, `lon` REAL NOT NULL, " +
                "PRIMARY KEY(`lat`, 'lon'))")
    }
}