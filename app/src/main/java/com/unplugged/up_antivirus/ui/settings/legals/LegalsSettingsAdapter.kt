package com.unplugged.up_antivirus.ui.settings.legals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unplugged.antivirus.R


class LegalsSettingsAdapter(private val legalsOnClickListener: LegalsOnClickListener) :
    ListAdapter<LegalItem, LegalsSettingsAdapter.ViewHolder>(TaskDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View
        when (viewType) {
            0 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_legals_item_title, parent, false)
                return ViewHolder(view)
            }

            1 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_legals_item, parent, false)
                return ViewHolder(view)
            }

            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_legals_item_title, parent, false)
            }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), legalsOnClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        when (getItem(position).type) {
            "title" -> {
                return 0
            }

            "item" -> {
                return 1
            }
        }
        return super.getItemViewType(position)
    }

    class TaskDiffCallBack : DiffUtil.ItemCallback<LegalItem>() {
        override fun areItemsTheSame(oldItem: LegalItem, newItem: LegalItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LegalItem, newItem: LegalItem): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)

        fun bind(setting: LegalItem, legalsOnClickListener: LegalsOnClickListener) {
            itemView.setOnClickListener {
                if (setting.type != "title") legalsOnClickListener.onLegalsItemClick(setting)
            }
            title.text = setting.text
        }
    }
}

