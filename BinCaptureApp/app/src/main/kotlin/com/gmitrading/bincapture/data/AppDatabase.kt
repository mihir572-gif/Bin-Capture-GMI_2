
package com.gmitrading.bincapture.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gmitrading.bincapture.data.dao.ItemDao
import com.gmitrading.bincapture.data.dao.RecordDao
import com.gmitrading.bincapture.data.entities.Item
import com.gmitrading.bincapture.data.entities.CaptureRecord

@Database(entities = [Item::class, CaptureRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun recordDao(): RecordDao
}
