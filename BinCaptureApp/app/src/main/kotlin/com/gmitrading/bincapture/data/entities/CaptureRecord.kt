
package com.gmitrading.bincapture.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class CaptureRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val username: String,
    val binLocation: String,
    val itemCode: String,
    val barcode: String,
    val description: String,
    val externalId: String?,
    val brand: String?,
    val qty: Int,
    val expirationDate: String
)
