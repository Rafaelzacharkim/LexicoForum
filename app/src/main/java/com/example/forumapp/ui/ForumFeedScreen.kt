package com.example.forumapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forumapp.R
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
    onCreatePostClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Comunidade", "Favoritos")

    var selectedPostId by remember { mutableStateOf<String?>(null) }
    val posts by viewModel.posts.collectAsState()

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
                            IconButton(onClick = { /*TODO: Perfil*/ }) {
                                Icon(
                                    // --- CORREÇÃO: Icons.Filled ---
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
                        // --- CORREÇÃO: Icons.Filled ---
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Criar novo post"
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {
            when (tabIndex) {
                0 -> {
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
                            viewModel = viewModel
                        )
                    }
                }
                1 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tela de Favoritos (Vazia)")
                    }
                }
            }
        }
    }
}

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

@Composable
fun PostDetailView(postId: String, viewModel: ForumViewModel) {

    LaunchedEffect(postId) {
        viewModel.getPostById(postId)
    }

    val post by viewModel.selectedPost.collectAsState()

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC9BAE1))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { /* TODO: Deletar */ }) {
                            Icon(
                                // --- CORREÇÃO: Icons.Filled ---
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Deletar"
                            )
                        }
                        IconButton(onClick = { /* TODO: Editar */ }) {
                            Icon(
                                // --- CORREÇÃO: Icons.Filled ---
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar"
                            )
                        }
                        IconButton(onClick = { /* TODO: Salvar/Favoritar */ }) {
                            Icon(
                                imageVector = Icons.Filled.FavoriteBorder,
                                contentDescription = "Salvar"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* TODO: Abrir comentários */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Faça seu comentário")
            }
        }
    }
}

@Composable
private fun FormatTimestamp(timestamp: Long): String {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val date = Date(timestamp)
        return sdf.format(date)
    } catch (e: Exception) {
        return "Data inválida"
    }
}