package am.clothesshop.global

import am.clothesshop.R
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StoresViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var storeName: TextView = itemView.findViewById(R.id.store_name)
    var storeAddress: TextView = itemView.findViewById(R.id.store_address)
    var storeImage: ImageView = itemView.findViewById(R.id.store_image)
}