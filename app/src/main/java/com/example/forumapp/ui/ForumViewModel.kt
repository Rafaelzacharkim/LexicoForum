package com.example.forumapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forumapp.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ForumViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Guarda a LISTA de posts para o feed
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    // --- NOVO: Guarda o POST ÚNICO selecionado ---
    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost
    // --- FIM NOVO ---

    init {
        fetchPosts()
    }

    // Carrega a LISTA de posts
    fun fetchPosts() {
        db.collection("posts")
            .orderBy("criado_em_", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("ForumViewModel", "Erro ao carregar posts.", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val postList = snapshots.toObjects(Post::class.java)
                    _posts.value = postList
                }
            }
    }

    // --- NOVO: Carrega um ÚNICO post pelo ID ---
    fun getPostById(postId: String) {
        viewModelScope.launch {
            try {
                // Limpa o post antigo para mostrar "Carregando..."
                _selectedPost.value = null
                // Busca o documento
                val doc = db.collection("posts").document(postId).get().await()
                // Converte e atualiza o StateFlow
                _selectedPost.value = doc.toObject(Post::class.java)
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Erro ao buscar post por ID", e)
                _selectedPost.value = null // Falha
            }
        }
    }
    // --- FIM NOVO ---

    // Cria um novo post (sem mudanças)
    fun createPost(title: String, content: String, onComplete: (Boolean) -> Unit) {
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
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Erro ao criar post", e)
                onComplete(false)
            }
        }
    }
}