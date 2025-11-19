
package com.gmitrading.bincapture.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemCode: String,
    val barcode: String,
    val description: String,
    val externalId: String?,
    val brand: String?
)
