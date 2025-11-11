package com.example.forumapp.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forumapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel, // Recebe o ViewModel
    postId: String?, // Recebe o ID (nulo se for "Criar")
    onNavigateBack: () -> Unit // Função para voltar
) {
    // Verifica se está em modo de edição
    val isEditing = postId != null

    // Observa o post selecionado (que o VM vai carregar)
    val postToEdit by viewModel.selectedPost.collectAsState()

    // Estados locais para o título e conteúdo
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Carrega os dados do post se estiver em modo de edição
    LaunchedEffect(key1 = postId, key2 = postToEdit) {
        if (isEditing && postId != null) {
            // 1. Pede ao VM para carregar o post
            viewModel.getPostById(postId)
            // 2. Se o post foi carregado, preenche os campos
            if (postToEdit != null && postToEdit!!.postId == postId) {
                title = postToEdit!!.topic
                content = postToEdit!!.content
            }
        }
    }

    // Cor de fundo lilás
    val lightPurpleBackground = Color(0xFFE6DDF3)

    Scaffold(
        topBar = {
            // ... (TopAppBar - sem mudanças)
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { /*TODO: Perfil*/ }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
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
        },
        containerColor = lightPurpleBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text(
                // Muda o título da tela
                text = if (isEditing) "Editar Post" else "Titulo do forum:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Coloque o seu texto:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("comente aqui....") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                readOnly = isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botão Cancelar
                Button(
                    onClick = { onNavigateBack() }, // Apenas volta
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancelar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }

                // Botão Concluir
                Button(
                    onClick = {
                        if (title.isBlank() || content.isBlank()) {
                            Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true

                        // --- LÓGICA DE DECISÃO ---
                        if (isEditing) {
                            // MODO DE EDIÇÃO
                            viewModel.updatePost(postId!!, title, content) { success ->
                                isLoading = false
                                if (success) {
                                    onNavigateBack() // Volta
                                } else {
                                    Toast.makeText(context, "Falha ao atualizar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // MODO DE CRIAÇÃO
                            viewModel.createPost(title, content) { success ->
                                isLoading = false
                                if (success) {
                                    onNavigateBack() // Volta
                                } else {
                                    Toast.makeText(context, "Falha ao criar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Concluir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Check, contentDescription = "Concluir")
                }
            }
        }
    }
}