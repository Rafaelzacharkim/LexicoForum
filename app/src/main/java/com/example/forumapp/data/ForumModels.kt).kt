package com.example.forumapp.data

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
)

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val topic: String = "Geral",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)