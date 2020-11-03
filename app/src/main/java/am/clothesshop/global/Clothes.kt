package am.clothesshop.global

class Clothes(
    private val name: String,
    private val description: String,
    private val price: String,
    private val size: String,
    private val image: String,
    private val store: String,
    private val address: String,
    private val type: String
) {

    fun getName(): String {
        return name
    }

    fun getDescription(): String {
        return description
    }

    fun getPrice(): String {
        return price
    }

    fun getSize(): String {
        return size
    }

    fun getImage(): String {
        return image
    }

    fun getStore(): String {
        return store
    }

    fun getAddress(): String {
        return address
    }

    fun getType(): String {
        return type
    }

    constructor() : this(
        "name",
        "description",
        "price",
        "size",
        "image",
        "stores",
        "address",
        "clothes"
    )
}