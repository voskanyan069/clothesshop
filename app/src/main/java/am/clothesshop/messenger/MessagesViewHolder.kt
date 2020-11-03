package am.clothesshop.messenger

import am.clothesshop.R
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val messageUsername: TextView = itemView.findViewById(R.id.message_username)
    val messageText: TextView = itemView.findViewById(R.id.message_text)
    val messageDate: TextView = itemView.findViewById(R.id.message_date)
    val messageImage: ImageView = itemView.findViewById(R.id.message_image)
}