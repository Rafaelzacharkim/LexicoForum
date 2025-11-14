package com.example.forumapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forumapp.data.Comment // <-- NOVO IMPORT
import com.example.forumapp.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ForumViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost

    private val _favoritePostIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePostIds: StateFlow<Set<String>> = _favoritePostIds

    private val _favoritePosts = MutableStateFlow<List<Post>>(emptyList())
    val favoritePosts: StateFlow<List<Post>> = _favoritePosts

    // --- NOVO: Para a lista de comentários ---
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private var favoritePostsListener: ListenerRegistration? = null
    // --- NOVO: Listener para comentários ---
    private var commentsListener: ListenerRegistration? = null

    init {
        fetchPosts()
        fetchUserFavorites()
    }

    // --- Funções de Post (sem mudanças) ---
    fun fetchPosts() {
        // ... (código igual)
        db.collection("posts")
            .orderBy("criado_em_", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) { /* ... */ return@addSnapshotListener }
                if (snapshots != null) {
                    _posts.value = snapshots.toObjects(Post::class.java)
                }
            }
    }
    fun getPostById(postId: String) {
        // ... (código igual)
        viewModelScope.launch {
            try {
                _selectedPost.value = null
                val doc = db.collection("posts").document(postId).get().await()
                _selectedPost.value = doc.toObject(Post::class.java)
            } catch (e: Exception) { /* ... */ }
        }
    }
    fun deletePost(postId: String, onComplete: (Boolean) -> Unit) {
        // ... (código igual)
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId).delete().await()
                _selectedPost.value = null
                onComplete(true)
            } catch (e: Exception) { onComplete(false) }
        }
    }
    fun createPost(title: String, content: String, onComplete: (Boolean) -> Unit) {
        // ... (código igual)
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch onComplete(false)
                val newPostRef = db.collection("posts").document()
                val post = Post(
                    postId = newPostRef.id,
                    authorId = user.uid,
                    authorName = user.displayName ?: user.email ?: "Anônimo",
                    topic = title,
                    content = content,
                    criado_em_ = System.currentTimeMillis()
                )
                newPostRef.set(post).await()
                onComplete(true)
            } catch (e: Exception) { onComplete(false) }
        }
    }
    fun updatePost(postId: String, newTopic: String, newContent: String, onComplete: (Boolean) -> Unit) {
        // ... (código igual)
        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                postRef.update(
                    "topic", newTopic,
                    "content", newContent
                ).await()
                onComplete(true)
            } catch (e: Exception) { onComplete(false) }
        }
    }

    // --- Funções de Favoritos (sem mudanças) ---
    fun toggleFavorite(postId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("ForumViewModel", "Usuário não autenticado")
            return
        }

        val favRef = db.collection("users").document(userId)
            .collection("favorites").document(postId)

        viewModelScope.launch {
            try {
                val isCurrentlyFavorite = _favoritePostIds.value.contains(postId)
                Log.d("ForumViewModel", "toggleFavorite - postId: $postId, isCurrentlyFavorite: $isCurrentlyFavorite")

                if (isCurrentlyFavorite) {
                    // Remove dos favoritos
                    favRef.delete().await()
                    Log.d("ForumViewModel", "Removido dos favoritos")
                } else {
                    // Adiciona aos favoritos
                    favRef.set(mapOf("addedAt" to System.currentTimeMillis())).await()
                    Log.d("ForumViewModel", "Adicionado aos favoritos")
                }
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Erro ao favoritar/desfavoritar", e)
            }
        }
    }
    private fun fetchUserFavorites() {
        // ... (código igual)
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("favorites")
            .addSnapshotListener { snapshots, error ->
                if (error != null) { return@addSnapshotListener }
                if (snapshots != null) {
                    val ids = snapshots.documents.map { it.id }.toSet()
                    _favoritePostIds.value = ids
                    fetchFavoritePosts(ids.toList())
                }
            }
    }
    private fun fetchFavoritePosts(ids: List<String>) {
        // ... (código igual)
        favoritePostsListener?.remove()
        if (ids.isEmpty()) {
            _favoritePosts.value = emptyList()
            return
        }
        favoritePostsListener = db.collection("posts")
            .whereIn("postId", ids)
            .addSnapshotListener { postSnapshots, error ->
                if (error != null) { /* ... */ return@addSnapshotListener }
                if (postSnapshots != null) {
                    val favPosts = postSnapshots.toObjects(Post::class.java)
                    _favoritePosts.value = favPosts.sortedByDescending { it.criado_em_ }
                }
            }
    }

    // --- NOVO: Funções de Comentários ---

    // Carrega os comentários de um post específico
    fun fetchComments(postId: String) {
        // Limpa o listener antigo
        commentsListener?.remove()

        // Path: /posts/{postId}/comments
        commentsListener = db.collection("posts").document(postId).collection("comments")
            .orderBy("criado_em_", Query.Direction.ASCENDING) // Do mais antigo ao mais novo
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("ForumViewModel", "Erro ao carregar comentários", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    _comments.value = snapshots.toObjects(Comment::class.java)
                }
            }
    }

    // Limpa os comentários da memória (chamado ao sair da tela de detalhe)
    fun clearComments() {
        commentsListener?.remove()
        _comments.value = emptyList()
    }

    // Cria um novo comentário
    fun createComment(postId: String, content: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null || content.isBlank()) {
                onComplete(false)
                return@launch
            }

            try {
                val newCommentRef = db.collection("posts").document(postId)
                    .collection("comments").document()

                val comment = Comment(
                    commentId = newCommentRef.id,
                    authorId = user.uid,
                    authorName = user.displayName ?: user.email ?: "Anônimo",
                    content = content,
                    criado_em_ = System.currentTimeMillis()
                )

                newCommentRef.set(comment).await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Erro ao criar comentário", e)
                onComplete(false)
            }
        }
    }

    // Deleta um comentário
    fun deleteComment(postId: String, commentId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId)
                    .collection("comments").document(commentId)
                    .delete().await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Erro ao deletar comentário", e)
                onComplete(false)
            }
        }
    }

    // Edita um comentário
    fun updateComment(postId: String, commentId: String, newContent: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId)
                    .collection("comments").document(commentId)
                    .update("content", newContent).await() // Atualiza só o conteúdo
                onComplete(true)
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Erro ao atualizar comentário", e)
                onComplete(false)
            }
        }
    }
}