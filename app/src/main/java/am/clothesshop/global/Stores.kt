package am.clothesshop.global

class Stores(
    private val name: String,
    private val description: String,
    private val image: String,
    private val address: String,
    private val followers: String
) {

    fun getName(): String {
        return name
    }

    fun getDescription(): String {
        return description
    }

    fun getImage(): String {
        return image
    }

    fun getAddress(): String {
        return address
    }

    fun getFollowers(): String {
        return followers
    }

    constructor() : this("name",
        "description",
        "image",
        "address",
        "followers")

}