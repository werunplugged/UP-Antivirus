package com.unplugged.up_antivirus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unplugged.signature_scanner.database.BlacklistedPackageDao
import com.unplugged.signature_scanner.model.BlacklistPackageEntity
import com.unplugged.up_antivirus.data.history.HistoryDao
import com.unplugged.up_antivirus.data.history.model.HistoryEntity
import com.unplugged.up_antivirus.data.malware.MalwareDao
import com.unplugged.up_antivirus.data.malware.model.MalwareEntity
import com.unplugged.up_antivirus.data.tracker.TrackerDao
import com.unplugged.up_antivirus.data.tracker.model.TrackerEntity
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [HistoryEntity::class, MalwareEntity::class, BlacklistPackageEntity::class, TrackerEntity::class],
    version = 3, exportSchema = false
)
@TypeConverters(TrackerListConverter::class)
@Singleton
abstract class AntivirusRoomDatabase : RoomDatabase() {
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getMalwareDao(): MalwareDao
    abstract fun getBlacklistPackageDao(): BlacklistedPackageDao
    abstract fun getTrackerDao(): TrackerDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMigration2_3(): Migration {
        return object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX index_MalwareEntity_scanId_filePath ON MalwareEntity (scanId, filePath)")
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `TrackerEntity` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `scanId` INTEGER NOT NULL,
                `appName` TEXT NOT NULL,
                `packageId` TEXT NOT NULL,
                `trackers` TEXT NOT NULL
            )
            """.trimIndent()
                )

                db.execSQL("DROP TABLE IF EXISTS HistoryEntity")

                // Create new table with updated schema
                db.execSQL(
                    """
            CREATE TABLE HistoryEntity (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                date TEXT NOT NULL,
                malwareFound INTEGER NOT NULL,
                trackersFound INTEGER NOT NULL,
                filesScanned INTEGER NOT NULL,
                megabytesHashed INTEGER NOT NULL
            )
        """
                )
            }
        }
    }

    fun provideMigration1_2(): Migration {
        return object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS BlacklistPackageEntity (id INTEGER PRIMARY KEY AUTOINCREMENT, sha256 TEXT NOT NULL, size INTEGER NOT NULL, packageName TEXT NOT NULL)")
            }
        }
    }

    @Provides
    @Singleton
    fun provideAntivirusRoomDatabase(
        @ApplicationContext context: Context,
        migration: Migration
    ): AntivirusRoomDatabase {
        return Room.databaseBuilder(
            context,
            AntivirusRoomDatabase::class.java, "antivirus_db"
        ).addMigrations(migration)
            .build()
    }
}