package am.clothesshop.messenger

class Message (
    private val messageUsername: String,
    private val messageText: String,
    private val messageDate: String,
    private val messageImage: String
) {
    fun getMessageUsername(): String {
        return messageUsername
    }

    fun getMessageText(): String {
        return messageText
    }

    fun getMessageDate(): String {
        return messageDate
    }

    fun getMessageImage(): String {
        return messageImage
    }

    constructor(): this (
        "Username",
        "Message",
        "22/10/2020 23:23",
        "image"
    )
}