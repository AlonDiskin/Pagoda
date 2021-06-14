package com.diskin.alon.pagoda

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `bookmarked_locations` (`lat` REAL NOT NULL, `lon` REAL NOT NULL, " +
                "PRIMARY KEY(`lat`, 'lon'))")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Delete bookmarked locations table
        database.execSQL("DROP TABLE bookmarked_locations")

        // Add 'bookmarked' column to locations table
        database.execSQL("ALTER TABLE locations ADD COLUMN bookmarked INTEGER NOT NULL DEFAULT 0")
    }
}