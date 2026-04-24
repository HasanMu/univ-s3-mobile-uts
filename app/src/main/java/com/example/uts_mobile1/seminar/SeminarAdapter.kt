package com.example.uts_mobile1.seminar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_mobile1.databinding.ItemSeminarBinding
import com.example.uts_mobile1.ui.SeminarItem

/**
 * RecyclerView Adapter for Seminar List
 *
 * Displays seminar items in a RecyclerView
 */
class SeminarAdapter(
    private val seminars: List<SeminarItem>,
    private val onItemClick: (SeminarItem) -> Unit
) : RecyclerView.Adapter<SeminarAdapter.SeminarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeminarViewHolder {
        val binding = ItemSeminarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SeminarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeminarViewHolder, position: Int) {
        holder.bind(seminars[position])
    }

    override fun getItemCount(): Int = seminars.size

    inner class SeminarViewHolder(
        private val binding: ItemSeminarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(seminars[position])
                }
            }
        }

        fun bind(seminar: SeminarItem) {
            binding.apply {
                tvTitle.text = seminar.title
                tvDescription.text = seminar.description
                tvDate.text = seminar.date
                tvLocation.text = seminar.location
                tvPrice.text = seminar.price
            }
        }
    }
}