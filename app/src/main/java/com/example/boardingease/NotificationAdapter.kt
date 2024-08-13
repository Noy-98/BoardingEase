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

class NotificationAdapter(private val context: Context, private var notificationList: List<TenantsReservedDataStructureDB>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tenantLastname: TextView = itemView.findViewById(R.id.tenant_lastname)
            val viewDetailsBttn: AppCompatButton = itemView.findViewById(R.id.viewDetails_bttn)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.landlord_notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]

        holder.tenantLastname.text = notification.landlord_lastname

        // Handle click on view details button
        holder.viewDetailsBttn.setOnClickListener {
            val intent = Intent(context, LandlordNotificationViewDetailsDashboard::class.java)
            intent.putExtra("reserved_id", notification.reserved_id)
            intent.putExtra("notificationData", notification)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    fun filterList(filteredList: List<TenantsReservedDataStructureDB>) {
        notificationList = filteredList.toMutableList()
        notifyDataSetChanged()
    }
}