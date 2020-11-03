package am.clothesshop.messenger;

class Chats(
    private var username: String,
    private var profileImage: String
) {
    fun getUsername(): String {
        return username
    }

    fun getProfileImage(): String {
        return profileImage
    }

    constructor(): this (
        "username",
        "image"
    )
}
