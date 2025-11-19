
package com.gmitrading.bincapture.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.gmitrading.bincapture.R
import com.gmitrading.bincapture.data.AppDatabase
import com.gmitrading.bincapture.data.entities.CaptureRecord
import kotlinx.coroutines.launch

class RecordsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bin_capture_db").build()
        val rv = findViewById<RecyclerView>(R.id.rvItems)
        rv.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            val records = db.recordDao().getAll()
            rv.adapter = RecordsAdapter(records)
        }
    }

    class RecordsAdapter(private val records: List<CaptureRecord>) : RecyclerView.Adapter<RecordsAdapter.VH>() {
        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.tvTitle)
            val subtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
            return VH(v)
        }
        override fun getItemCount(): Int = records.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = records[position]
            holder.title.text = "${r.binLocation} • ${r.itemCode} (${r.qty})"
            holder.subtitle.text = "${r.description} • Exp: ${r.expirationDate} • ${r.barcode}"
        }
    }
}
