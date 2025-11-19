
package com.gmitrading.bincapture.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gmitrading.bincapture.data.entities.Item

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Item>)

    @Query("SELECT * FROM items WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): Item?

    @Query("SELECT * FROM items WHERE itemCode LIKE '%' || :q || '%' OR barcode LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' OR externalId LIKE '%' || :q || '%' OR brand LIKE '%' || :q || '%' ORDER BY description LIMIT 100")
    suspend fun search(q: String): List<Item>

    @Query("DELETE FROM items")
    suspend fun clear()
}
