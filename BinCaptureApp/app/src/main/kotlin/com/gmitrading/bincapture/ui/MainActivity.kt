
package com.gmitrading.bincapture.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.gmitrading.bincapture.R
import com.gmitrading.bincapture.data.AppDatabase
import com.gmitrading.bincapture.data.entities.CaptureRecord
import com.gmitrading.bincapture.data.entities.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var tvUser: TextView
    private lateinit var tvBin: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvSelectedItem: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var etQty: EditText
    private lateinit var btnPickDate: Button
    private lateinit var tvDate: TextView
    private lateinit var btnSave: Button

    private var username: String = "DeviceUser"
    private var binLocation: String = ""
    private var selectedItem: Item? = null
    private var expirationDateStr: String = ""

    private val scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (data != null) {
            val contents = data.getStringExtra("SCAN_RESULT")
            if (contents != null) {
                // Determine if we were scanning a bin or UPC by checking current focus
                if (tvBin.hasFocus()) {
                    binLocation = contents
                    tvBin.text = "Bin: $binLocation"
                } else {
                    // UPC scan: lookup item
                    lifecycleScope.launch {
                        val item = withContext(Dispatchers.IO) { db.itemDao().getByBarcode(contents) }
                        if (item != null) {
                            selectedItem = item
                            tvSelectedItem.text = "Selected: ${item.description} (${item.itemCode})"
                        } else {
                            Toast.makeText(this@MainActivity, "UPC not found in item file", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bin_capture_db").build()

        tvUser = findViewById(R.id.tvUser)
        tvBin = findViewById(R.id.tvBin)
        etSearch = findViewById(R.id.etSearch)
        tvSelectedItem = findViewById(R.id.tvSelectedItem)
        rvItems = findViewById(R.id.rvItems)
        etQty = findViewById(R.id.etQty)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvDate = findViewById(R.id.tvDate)
        btnSave = findViewById(R.id.btnSave)

        rvItems.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnSetUser).setOnClickListener {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Set User")
                .setView(EditText(this).apply { setText(username) })
                .setPositiveButton("Save") { d, _ ->
                    val et = (d as androidx.appcompat.app.AlertDialog).findViewById<EditText>(androidx.appcompat.R.id.alertTitle)
                    // Fallback: get text from custom view
                    username = "User"
                    tvUser.text = "User: $username"
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }

        findViewById<Button>(R.id.btnScanBin).setOnClickListener {
            val integrator = com.journeyapps.barcodescanner.IntentIntegrator(this)
            integrator.setPrompt("Scan Bin Location")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }

        findViewById<Button>(R.id.btnScanUPC).setOnClickListener {
            val integrator = com.journeyapps.barcodescanner.IntentIntegrator(this)
            integrator.setPrompt("Scan UPC")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }

        etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                lifecycleScope.launch {
                    val items = withContext(Dispatchers.IO) { db.itemDao().search(q) }
                    rvItems.adapter = ItemAdapter(items) { item ->
                        selectedItem = item
                        tvSelectedItem.text = "Selected: ${item.description} (${item.itemCode})"
                    }
                }
            }
        })

        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                cal.set(y, m, d)
                expirationDateStr = sdf.format(cal.time)
                tvDate.text = "Date: $expirationDateStr"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSave.setOnClickListener {
            val qtyStr = etQty.text.toString()
            val qty = qtyStr.toIntOrNull() ?: 0
            val item = selectedItem
            if (binLocation.isEmpty()) {
                Toast.makeText(this, "Scan Bin first", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (item == null) { Toast.makeText(this, "Select an item", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (qty <= 0) { Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (expirationDateStr.isEmpty()) { Toast.makeText(this, "Pick expiration date", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.recordDao().insert(
                        CaptureRecord(
                            timestamp = System.currentTimeMillis(),
                            username = username,
                            binLocation = binLocation,
                            itemCode = item.itemCode,
                            barcode = item.barcode,
                            description = item.description,
                            externalId = item.externalId,
                            brand = item.brand,
                            qty = qty,
                            expirationDate = expirationDateStr
                        )
                    )
                }
                Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnImport).setOnClickListener {
            startActivity(Intent(this, ImportItemsActivity::class.java))
        }
        findViewById<Button>(R.id.btnRecords).setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }
        findViewById<Button>(R.id.btnExport).setOnClickListener {
            exportCsv()
        }
    }

    private fun exportCsv() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) { db.recordDao().getAll() }
            val header = "Timestamp,User,Bin Location,Item Code,Barcode,Description,External ID,Brand,Qty,Expiration Date
"
            val sb = StringBuilder(header)
            val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            for (r in records) {
                val ts = dateFmt.format(java.util.Date(r.timestamp))
                val row = listOf(ts, r.username, r.binLocation, r.itemCode, r.barcode, r.description, r.externalId ?: "", r.brand ?: "", r.qty.toString(), r.expirationDate)
                sb.append(row.joinToString(",") { it.replace(""", """") }).append("
")
            }
            // Create file via SAF
            val createFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { it.write(sb.toString().toByteArray()) }
                    Toast.makeText(this, "CSV exported", Toast.LENGTH_LONG).show()
                }
            }
            val fname = "bin-capture-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(java.util.Date()) + ".csv"
            createFile.launch(fname)
        }
    }

    class ItemAdapter(private val items: List<Item>, private val onClick: (Item) -> Unit) : RecyclerView.Adapter<ItemAdapter.VH>() {
        class VH(v: android.view.View): RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tvTitle)
            val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
        }
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = items.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val it = items[position]
            holder.title.text = "${it.description}"
            holder.subtitle.text = "${it.itemCode} • ${it.barcode} • ${it.brand ?: ""}"
            holder.itemView.setOnClickListener { onClick(it) }
        }
    }
}
