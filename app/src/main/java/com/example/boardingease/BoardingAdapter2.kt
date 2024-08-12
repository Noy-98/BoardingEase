package com.example.boardingease


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BoardingAdapter2(private val context: Context, private var boardingList: List<BoardingDataStructureDB>) :
    RecyclerView.Adapter<BoardingAdapter2.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val boardingHouse_pic: ImageView = itemView.findViewById(R.id.boardinghouse_pic)
            val landlordLastname: TextView = itemView.findViewById(R.id.landlord_lastname)
            val stat: TextView = itemView.findViewById(R.id.status)
            val pricee: TextView = itemView.findViewById(R.id.price)
            val viewDetailsBttn: AppCompatButton = itemView.findViewById(R.id.viewDetails_bttn)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.tenants_home_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val boarding = boardingList[position]

        // Load image from URL using Glide
        Glide.with(context)

            .load(boarding.unitPictureUrl) // Assuming imageUrl is the field in your BoardingDataStructureDB class
            .placeholder(R.drawable.boardingease_logo_icon) // Placeholder image while loading
            .error(R.drawable.boardingease_logo_icon) // Error image if the loading fails
            .into(holder.boardingHouse_pic)

        holder.landlordLastname.text = boarding.landlord_lastname
        holder.stat.text = boarding.status
        holder.pricee.text = boarding.price

        // Handle click on view details button
        holder.viewDetailsBttn.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return boardingList.size
    }

    fun filterList(filteredList: List<BoardingDataStructureDB>) {
        boardingList = filteredList.toMutableList()
        notifyDataSetChanged()
    }
}