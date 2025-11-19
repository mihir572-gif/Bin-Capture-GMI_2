
package com.gmitrading.bincapture.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gmitrading.bincapture.data.entities.CaptureRecord

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: CaptureRecord)

    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    suspend fun getAll(): List<CaptureRecord>

    @Query("DELETE FROM records")
    suspend fun clear()
}
