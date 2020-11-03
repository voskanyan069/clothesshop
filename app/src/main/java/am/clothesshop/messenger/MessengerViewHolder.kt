package am.clothesshop.messenger

import am.clothesshop.R
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessengerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val chatName: TextView = itemView.findViewById(R.id.chat_name)
    val chatImage: ImageView = itemView.findViewById(R.id.chat_image)
}