package com.example.arfaceexample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arfaceexample.R
import com.example.arfaceexample.model.Filter
import kotlinx.android.synthetic.main.item_ar_face.view.*

class ListArFace(private val listFilter: ArrayList<Filter>) :
    RecyclerView.Adapter<ListArFace.ListViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Filter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ar_face, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listFilter.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(listFilter[position])
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(filter: Filter) {
            with(itemView) {

                textView_name_filter.text = filter.name

                itemView.setOnClickListener { onItemClickCallback?.onItemClicked(filter)}
            }
        }

    }
}