package com.unplugged.up_antivirus.ui.scan

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.data.tracker.model.ExpandableItem

class ExpandableTrackerAdapter :
    ListAdapter<ExpandableItem, ExpandableTrackerAdapter.ExpandableViewHolder>(DiffCallback()) {

    class ExpandableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerLayout: ConstraintLayout = itemView.findViewById(R.id.header_layout)
        val title: TextView = itemView.findViewById(R.id.title)
        val mainTag: TextView = itemView.findViewById(R.id.tag)
        val mainTagLayout: ConstraintLayout = itemView.findViewById(R.id.main_tag_layout)
        val extraTagsNumber: TextView = itemView.findViewById(R.id.extra_tags_number)
        val arrow: ImageView = itemView.findViewById(R.id.arrow)
        val expandableLayout: ConstraintLayout = itemView.findViewById(R.id.sub_items_layout)
        val description: TextView = itemView.findViewById(R.id.description)
        val tvLinksTitle: TextView = itemView.findViewById(R.id.tv_links_title)
        val links: TextView = itemView.findViewById(R.id.tv_links)
        val tagsRecycler: RecyclerView = itemView.findViewById(R.id.tags_recycler)
        val divider: LinearLayout = itemView.findViewById(R.id.divider)

        fun bind(item: ExpandableItem) {
            title.text = item.tracker.name
            mainTag.text = item.tracker.tags.first()
            if (item.tracker.tags.size > 1) {
                extraTagsNumber.isVisible = true
                extraTagsNumber.text = "+${item.tracker.tags.size - 1}"
            } else {
                extraTagsNumber.isVisible = false
            }

            description.text = item.tracker.description

            if (item.isOpen) {
                arrow.setImageResource(R.drawable.ic_arrow_drop_up_green)
                expandableLayout.isVisible = true
                mainTagLayout.isVisible = false
            } else {
                arrow.setImageResource(R.drawable.ic_arrow_drop_down_green)
                expandableLayout.isVisible = false
                mainTagLayout.isVisible = true
            }

            if (item.tracker.links.isEmpty()) {
                tvLinksTitle.isVisible = false
                links.isVisible = false
            } else {
                tvLinksTitle.isVisible = true
                links.isVisible = true
                links.makeLinksClickable(item.tracker.links)

                    HtmlCompat.fromHtml(item.tracker.links, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }

            tagsRecycler.adapter = TagsAdapter(item.tracker.tags)
        }

        fun TextView.makeLinksClickable(text: String) {
            try {
                val spanned = SpannableString(text)
                val matcher = Patterns.WEB_URL.matcher(text)
                var matchStart: Int
                var matchEnd: Int

                while (matcher.find()) {
                    matchStart = matcher.start(1)
                    matchEnd = matcher.end()

                    var url = text.substring(matchStart, matchEnd)
                    if (!url.startsWith("https://)") && !url.startsWith("https://")) {
                        url = "https://$url"
                    }

                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(context, browserIntent, null)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                        }
                    }

                    spanned.setSpan(
                        clickableSpan,
                        matchStart,
                        matchEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    this.text = spanned
                    this.movementMethod = LinkMovementMethod.getInstance()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandableViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expandable_trackers, parent, false)
        return ExpandableViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpandableViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

        holder.headerLayout.setOnClickListener {
            val transitionSet = TransitionSet()
            val autoTransition = AutoTransition()
            transitionSet.addTransition(autoTransition)
            val slide = Slide(Gravity.TOP)
            transitionSet.addTransition(slide)
            transitionSet.duration = 200
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup, transitionSet)
            currentItem.isOpen = !currentItem.isOpen
            notifyItemChanged(position)
        }

        if (position == itemCount - 1) {
            holder.divider.isVisible = false
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpandableItem>() {
        override fun areItemsTheSame(oldItem: ExpandableItem, newItem: ExpandableItem): Boolean {
            return oldItem.isOpen == newItem.isOpen
        }

        override fun areContentsTheSame(oldItem: ExpandableItem, newItem: ExpandableItem): Boolean {
            return oldItem == newItem
        }
    }
}