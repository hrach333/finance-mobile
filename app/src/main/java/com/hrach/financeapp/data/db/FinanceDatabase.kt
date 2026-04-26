package com.hrach.financeapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hrach.financeapp.data.db.converter.LocalDateTimeConverter
import com.hrach.financeapp.data.db.dao.LocalFinanceDao
import com.hrach.financeapp.data.db.dao.PendingOperationDao
import com.hrach.financeapp.data.db.entity.LocalAccountEntity
import com.hrach.financeapp.data.db.entity.LocalCategoryEntity
import com.hrach.financeapp.data.db.entity.LocalGroupEntity
import com.hrach.financeapp.data.db.entity.LocalTransactionEntity
import com.hrach.financeapp.data.db.entity.PendingOperationEntity

/**
 * Room Database для хранения офлайн операций
 */
@Database(
    entities = [
        PendingOperationEntity::class,
        LocalGroupEntity::class,
        LocalCategoryEntity::class,
        LocalAccountEntity::class,
        LocalTransactionEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class FinanceDatabase : RoomDatabase() {
    
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun localFinanceDao(): LocalFinanceDao
    
    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS local_groups (
                        id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        baseCurrency TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS local_categories (
                        id INTEGER NOT NULL PRIMARY KEY,
                        groupId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        name TEXT NOT NULL,
                        iconKey TEXT,
                        isSystem INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS local_accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        groupId INTEGER NOT NULL,
                        userId INTEGER,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        currency TEXT NOT NULL,
                        initialBalance REAL NOT NULL,
                        currentBalance REAL NOT NULL,
                        shared INTEGER NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS local_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        groupId INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        createdBy INTEGER,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        categoryId INTEGER,
                        transactionDate TEXT NOT NULL,
                        comment TEXT
                    )
                    """.trimIndent()
                )
            }
        }
        
        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
