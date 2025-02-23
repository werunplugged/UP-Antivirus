package com.unplugged.up_antivirus.ui.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.unplugged.antivirus.R
import com.unplugged.upantiviruscommon.utils.DateTimeUtils
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import java.text.SimpleDateFormat
import java.util.Locale


class ScanHistoryAdapter(
    private val clickListener: (item: HistoryModel) -> Unit,
    private val historyItems: MutableList<HistoryModel> = mutableListOf()
) : RecyclerView.Adapter<ScanHistoryAdapter.HistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        return HistoryHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = historyItems[position]

        holder.scanTitle.text = item.name
        holder.scanDate.text = item.date
        holder.threatsFound.text = String.format(getString(holder.itemView.context, R.string.up_av_threats_found), item.malwareFound, item.trackersFound)

        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }

    override fun getItemCount(): Int {
        return historyItems.count()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setHistory(history: List<HistoryModel>) {
        historyItems.clear()
        historyItems.addAll(history)
        notifyDataSetChanged()
    }

    class HistoryHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scanTitle: TextView = view.findViewById(R.id.scan_title)
        val scanDate: TextView = view.findViewById(R.id.scan_date)
        val threatsFound: TextView = view.findViewById(R.id.threats_found)
    }
}