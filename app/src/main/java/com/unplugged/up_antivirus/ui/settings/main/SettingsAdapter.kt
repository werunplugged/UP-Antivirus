package com.unplugged.up_antivirus.ui.settings.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.ui.settings.SettingsOnClickListener


class SettingsAdapter(private val settingsOnClickListener: SettingsOnClickListener) : ListAdapter<String, SettingsAdapter.ViewHolder>(
    TaskDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_item, parent, false)
        when (viewType) {
            0 -> {
                return ViewHolder(view)
            }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), settingsOnClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        when (getItem(position)) {
            is String -> {
                return 0
            }
        }
        return super.getItemViewType(position)
    }

    class TaskDiffCallBack : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val title: TextView = itemView.findViewById(R.id.title)
        fun bind(setting: String, settingsOnClickListener: SettingsOnClickListener) {

            itemView.setOnClickListener {
                settingsOnClickListener.onMainItemClick(setting)
            }

            title.text = setting
            when(setting){
                "Account"->{
                    icon.setImageDrawable(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_settings_18dp))
                }
                "Scheduler"->{
                    icon.setImageDrawable(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_settings_scheduler))
                }
                "Support"->{
                    icon.setImageDrawable(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_support))
                }
                "Legals"->{
                    icon.setImageDrawable(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_settings_root_legals))
                }
            }
        }
    }
}

