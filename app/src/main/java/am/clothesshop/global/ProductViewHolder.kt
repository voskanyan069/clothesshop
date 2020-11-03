package am.clothesshop.global

import am.clothesshop.R
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val productName: TextView = itemView.findViewById(R.id.product_name)
    val productDescription: TextView = itemView.findViewById(R.id.product_description)
    val productPrice: TextView = itemView.findViewById(R.id.product_price)
    val productStore: TextView = itemView.findViewById(R.id.product_store)
    val productImage: ImageView = itemView.findViewById(R.id.product_image)
    val productLikeImage: ImageView = itemView.findViewById(R.id.product_like_image)
}