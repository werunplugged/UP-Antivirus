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
import com.unplugged.up_antivirus.data.history.model.ScanStatus
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

        // A scan is only titled once it finishes, so cancelled and interrupted rows have a
        // blank name. Fall back to a generic label rather than rendering an empty row.
        holder.scanTitle.text = item.name.ifBlank {
            getString(holder.itemView.context, R.string.up_av_scan_title_generic)
        }
        holder.scanDate.text = item.date
        // A scan that never finished has no meaningful counts to show, so say why instead.
        holder.threatsFound.text = when (item.status) {
            ScanStatus.CANCELLED -> getString(holder.itemView.context, R.string.up_av_scan_cancelled)
            ScanStatus.RUNNING,
            ScanStatus.INTERRUPTED -> getString(holder.itemView.context, R.string.up_av_scan_interrupted)
            ScanStatus.COMPLETED -> String.format(
                getString(holder.itemView.context, R.string.up_av_threats_found),
                item.malwareFound,
                item.trackersFound
            )
        }

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