package com.hrach.financeapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hrach.financeapp.data.db.converter.LocalDateTimeConverter
import com.hrach.financeapp.data.db.dao.PendingOperationDao
import com.hrach.financeapp.data.db.entity.PendingOperationEntity

/**
 * Room Database для хранения офлайн операций
 */
@Database(
    entities = [PendingOperationEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class FinanceDatabase : RoomDatabase() {
    
    abstract fun pendingOperationDao(): PendingOperationDao
    
    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null
        
        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
