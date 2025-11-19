
package com.gmitrading.bincapture.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.gmitrading.bincapture.R
import com.gmitrading.bincapture.data.AppDatabase
import com.gmitrading.bincapture.data.entities.Item
import com.gmitrading.bincapture.util.CsvUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportItemsActivity : AppCompatActivity() {
    private val REQUEST_PICK_FILE = 1001
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // reuse simple layout with buttons

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bin_capture_db").build()

        findViewById<Button>(R.id.btnImport).apply {
            text = getString(R.string.select_file)
            setOnClickListener { pickFile() }
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        startActivityForResult(intent, REQUEST_PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> importCsv(uri) }
        }
    }

    private fun importCsv(uri: Uri) {
        lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                val items = mutableListOf<Item>()
                contentResolver.openInputStream(uri)?.use { input ->
                    BufferedReader(InputStreamReader(input)).use { reader ->
                        var headerParsed = false
                        var line: String?
                        var columns: List<String> = emptyList()
                        while (true) {
                            line = reader.readLine()
                            if (line == null) break
                            if (!headerParsed) {
                                columns = CsvUtil.parseLine(line)
                                headerParsed = true
                                continue
                            }
                            val fields = CsvUtil.parseLine(line)
                            fun col(name: String): String? {
                                val idx = columns.indexOfFirst { it.trim().equals(name, ignoreCase = true) }
                                return if (idx >= 0 && idx < fields.size) fields[idx].trim() else null
                            }
                            val itemCode = col("Item Code") ?: col("ItemCode") ?: ""
                            val barcode = col("Barcode") ?: col("UPC") ?: ""
                            val description = col("Description") ?: ""
                            val externalId = col("External ID") ?: col("ExternalID")
                            val brand = col("Brand") ?: col("Bran")
                            if (barcode.isNotEmpty() || itemCode.isNotEmpty()) {
                                items.add(Item(
                                    itemCode = itemCode,
                                    barcode = barcode,
                                    description = description,
                                    externalId = externalId,
                                    brand = brand
                                ))
                            }
                        }
                    }
                }
                db.itemDao().clear()
                if (items.isNotEmpty()) db.itemDao().insert(items)
                items.size
            }
            Toast.makeText(this@ImportItemsActivity, "Imported $count items", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
