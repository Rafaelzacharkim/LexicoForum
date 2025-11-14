package com.example.forumapp.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forumapp.R
import com.example.forumapp.data.Comment // <-- NOVO IMPORT
import com.example.forumapp.data.Post
import com.example.forumapp.theme.ForumAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel,
    onCreatePostClick: () -> Unit,
    onEditPostClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Comunidade", "Favoritos")

    var selectedPostId by remember { mutableStateOf<String?>(null) }

    val posts by viewModel.posts.collectAsState()
    val favoriteIds by viewModel.favoritePostIds.collectAsState()
    val favoritePosts by viewModel.favoritePosts.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        scope.launch {
                                            lazyListState.animateScrollToItem(0)
                                        }
                                        selectedPostId = null
                                        viewModel.clearComments() // <-- LIMPA COMENTÁRIOS
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_lexico),
                                    contentDescription = "Logo Léxico",
                                    modifier = Modifier.height(32.dp)
                                )
                                Text(
                                    text = "éxico",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = onProfileClick) {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Perfil",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                TabRow(
                    selectedTabIndex = tabIndex,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = {
                                tabIndex = index
                                selectedPostId = null
                                viewModel.clearComments() // <-- LIMPA COMENTÁRIOS
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedPostId == null) {
                FloatingActionButton(
                    onClick = onCreatePostClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Criar novo post"
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {
            when (tabIndex) {
                0 -> { // Comunidade
                    if (selectedPostId == null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = lazyListState
                        ) {
                            items(posts) { post ->
                                PostItemCard(
                                    post = post,
                                    onClick = { selectedPostId = post.postId }
                                )
                            }
                        }
                    } else {
                        PostDetailView(
                            postId = selectedPostId!!,
                            viewModel = viewModel,
                            isFavorite = favoriteIds.contains(selectedPostId),
                            onEditClick = { onEditPostClick(selectedPostId!!) }
                        )
                    }
                }
                1 -> { // Favoritos
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = lazyListState
                    ) {
                        items(favoritePosts) { post ->
                            PostItemCard(
                                post = post,
                                onClick = { selectedPostId = post.postId }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ... (PostItemCard - Sem mudanças)
@Composable
fun PostItemCard(post: Post, onClick: () -> Unit) {
    val lightPurpleCard = Color(0xFFC9BAE1)
    val formattedDate = FormatTimestamp(post.criado_em_)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = lightPurpleCard)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = post.topic,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "criado por: ${post.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "em: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


// --- PostDetailView ATUALIZADO ---
@Composable
fun PostDetailView(
    postId: String,
    viewModel: ForumViewModel,
    isFavorite: Boolean,
    onEditClick: () -> Unit
) {
    // Carrega o post E os comentários
    LaunchedEffect(postId) {
        viewModel.getPostById(postId)
        viewModel.fetchComments(postId)
    }

    val post by viewModel.selectedPost.collectAsState()
    val comments by viewModel.comments.collectAsState() // <-- Pega os comentários

    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentUserId = viewModel.currentUserId
    val context = LocalContext.current
    var showAdminMenu by remember { mutableStateOf(false) }

    // --- NOVO: Estado para controlar o pop-up de edição de comentário ---
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {

        if (showDeleteDialog) {
            // ... (AlertDialog de Deletar Post - Sem mudanças)
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Deletar Post") },
                text = { Text("Você tem certeza que quer deletar este post? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePost(post!!.postId) { success ->
                                if (!success) {
                                    Toast.makeText(context, "Falha ao deletar", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Deletar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // --- NOVO: Pop-up para Editar Comentário ---
        if (commentToEdit != null) {
            EditCommentDialog(
                comment = commentToEdit!!,
                onDismiss = { commentToEdit = null },
                onConfirm = { newContent ->
                    viewModel.updateComment(postId, commentToEdit!!.commentId, newContent) { success ->
                        if (!success) {
                            Toast.makeText(context, "Falha ao editar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    commentToEdit = null
                }
            )
        }

        // Layout principal: Post, Lista de Comentários, Caixa de Input
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Card do Post (igual)
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                // Removido .weight(1f) para o card não esticar
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC9BAE1))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ... (Título, Conteúdo, etc. - Sem mudanças)
                    Text(
                        text = post!!.topic,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = post!!.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "criado por: ${post!!.authorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "em: ${FormatTimestamp(post!!.criado_em_)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Espaço fixo

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ... (Botão de Favorito - Sem mudanças)
                        val icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
                        val tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else LocalContentColor.current
                        val animatedTint by animateColorAsState(targetValue = tint, label = "FavoriteColor")

                        IconButton(onClick = { viewModel.toggleFavorite(post!!.postId) }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Favoritar",
                                tint = animatedTint
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // ... (Menu Admin - Sem mudanças)
                        if (currentUserId == post!!.authorId) {
                            Box {
                                IconButton(onClick = { showAdminMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "Opções do post"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showAdminMenu,
                                    onDismissRequest = { showAdminMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        onClick = {
                                            showAdminMenu = false
                                            onEditClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showAdminMenu = false
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- NOVA: Lista de Comentários ---
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(comments) { comment ->
                    CommentItemCard(
                        comment = comment,
                        isAuthor = currentUserId == comment.authorId,
                        onEdit = { commentToEdit = comment }, // <-- Abre o pop-up de edição
                        onDelete = {
                            viewModel.deleteComment(postId, comment.commentId) { success ->
                                if (!success) {
                                    Toast.makeText(context, "Falha ao deletar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            // --- NOVA: Caixa de Input de Comentário ---
            CommentInputBox(
                onCommentSend = { content ->
                    viewModel.createComment(postId, content) { success ->
                        if (!success) {
                            Toast.makeText(context, "Falha ao enviar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

// --- NOVO COMPOSABLE: Card do Comentário ---
@Composable
fun CommentItemCard(
    comment: Comment,
    isAuthor: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Menu de 3 pontos (só para o autor)
            if (isAuthor) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opções do comentário")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(text = { Text("Editar") }, onClick = {
                            onEdit()
                            showMenu = false
                        })
                        DropdownMenuItem(text = { Text("Excluir") }, onClick = {
                            onDelete()
                            showMenu = false
                        })
                    }
                }
            }
        }
    }
}

// --- NOVO COMPOSABLE: Pop-up de Edição de Comentário ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommentDialog(
    comment: Comment,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newContent by remember { mutableStateOf(comment.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Comentário") },
        text = {
            OutlinedTextField(
                value = newContent,
                onValueChange = { newContent = it },
                label = { Text("Seu comentário") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(newContent) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// --- NOVO COMPOSABLE: Caixa de Input ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputBox(onCommentSend: (String) -> Unit) {
    var showInput by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        if (!showInput) {
            // 1. Botão Padrão
            Button(
                onClick = { showInput = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Faça seu comentário")
            }
        } else {
            // 2. Campo de Texto (aparece ao clicar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Escreva seu comentário...") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester), // Aplica o foco
                    trailingIcon = {
                        // Botão de Enviar
                        IconButton(onClick = {
                            if (commentText.isNotBlank()) {
                                onCommentSend(commentText)
                                commentText = "" // Limpa o campo
                                // showInput = false (Opcional: fechar o campo após enviar)
                            }
                        }) {
                            Icon(Icons.Filled.Send, contentDescription = "Enviar")
                        }
                    }
                )
            }

            // Pede o foco (e abre o teclado) assim que o campo aparece
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}


// --- Função de Data (sem mudanças) ---
@Composable
fun FormatTimestamp(timestamp: Long): String {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val date = Date(timestamp)
        return sdf.format(date)
    } catch (e: Exception) {
        return "Data inválida"
    }
}