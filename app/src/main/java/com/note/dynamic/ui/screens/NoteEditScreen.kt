package com.note.dynamic.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.note.dynamic.data.Attachment
import com.note.dynamic.data.Note
import com.note.dynamic.data.Palettes
import com.note.dynamic.ui.Format
import com.note.dynamic.ui.NotesViewModel
import com.note.dynamic.ui.queryFileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    presetGroupId: Long? = null,
    onBack: () -> Unit
) {
    val vm: NotesViewModel = viewModel(factory = NotesViewModel.Factory)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(noteId == 0L) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var existingGroupId by remember { mutableStateOf<Long?>(presetGroupId) }
    var currentId by remember { mutableStateOf(noteId) }

    // Load existing note
    LaunchedEffect(noteId) {
        if (noteId != 0L) {
            val n = vm.getNote(noteId)
            if (n != null) {
                title = n.title
                content = n.content
                colorIndex = n.colorIndex
                existingGroupId = n.groupId
                currentId = n.id
            }
            loaded = true
        }
    }

    // Persist (create if needed) so attachments can be linked to a real note id
    suspend fun ensureNoteSaved(): Long {
        if (currentId != 0L) return currentId
        val id = vm.saveNoteReturnId(
            Note(
                title = title.ifBlank { "无标题笔记" },
                content = content,
                groupId = presetGroupId,
                colorIndex = colorIndex
            )
        )
        currentId = id
        return id
    }

    // Guard against double-save when both the back arrow and the system back gesture fire.
    var saving by remember { mutableStateOf(false) }

    fun save() {
        if (saving) return
        saving = true
        scope.launch {
            // 只要输入了标题或内容，就保存；否则直接返回不保存。
            val hasInput = title.isNotBlank() || content.isNotBlank()
            if (hasInput) {
                val id = ensureNoteSaved()
                vm.saveNoteReturnId(
                    Note(
                        id = id,
                        title = title.ifBlank { "无标题笔记" },
                        content = content,
                        groupId = existingGroupId,
                        colorIndex = colorIndex
                    )
                )
            }
            onBack()
        }
    }

    // 让系统返回键 / 手势返回也走相同的保存逻辑
    BackHandler(enabled = !saving) { save() }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val id = ensureNoteSaved()
            uris.forEach { uri ->
                val info = withContext(Dispatchers.IO) { uri.queryFileInfo(context) }
                tryTakePermission(context, uri)
                vm.addImage(id, uri, info.name, info.mime, info.size)
            }
        }
    }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val id = ensureNoteSaved()
            uris.forEach { uri ->
                val info = withContext(Dispatchers.IO) { uri.queryFileInfo(context) }
                tryTakePermission(context, uri)
                vm.addFile(id, uri, info.name, info.mime, info.size)
            }
        }
    }

    // Always observe attachments for the current note id; returns empty list when id is 0.
    val attachments by vm.observeAttachments(currentId).collectAsStateWithLifecycle(emptyList())

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Palettes.noteTint(colorIndex),
                            onClick = { colorIndex = (colorIndex + 1) % Palettes.noteTints.size },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Palettes.noteAccent(colorIndex))
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (noteId == 0L) "新建笔记" else "编辑笔记",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "输入内容后返回将自动保存",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { save() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { pickImage.launch("image/*") }) {
                        Icon(Icons.Outlined.Image, contentDescription = "插入图片")
                    }
                    IconButton(onClick = { pickFile.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = "插入文件")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ---------- Title ----------
            item {
                Text(
                    text = "标题",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                PlainInput(
                    text = title,
                    onText = { title = it },
                    placeholder = "给笔记起个名字…",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Palettes.noteTint(colorIndex),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = Palettes.noteAccent(colorIndex), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "内容 · 支持插入本机图片和文件",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ---------- Content body ----------
            item {
                PlainInput(
                    text = content,
                    onText = { content = it },
                    placeholder = "在这里写下你的想法…\n\n点击右上角图标可插入本机图片或文件",
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                    minLines = 6
                )
            }

            // ---------- Image attachments ----------
            val images = attachments.filter { it.type == "image" }
            if (images.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "引用的图片 (${images.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(images, key = { it.id }) { img ->
                            ImageAttachmentCard(
                                att = img,
                                onDelete = { vm.deleteAttachment(img) }
                            )
                        }
                    }
                }
            }

            // ---------- File attachments ----------
            val files = attachments.filter { it.type == "file" }
            if (files.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "引用的文件 (${files.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    files.forEach { f ->
                        FileAttachmentRow(
                            att = f,
                            onDelete = { vm.deleteAttachment(f) }
                        )
                    }
                }
            }

            // ---------- Quick insert buttons ----------
            item {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { pickImage.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text("添加图片", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { pickFile.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text("添加文件", color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

private fun tryTakePermission(context: android.content.Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

@Composable
private fun PlainInput(
    text: String,
    onText: (String) -> Unit,
    placeholder: String,
    style: TextStyle,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = text,
        onValueChange = onText,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = style.copy(color = MaterialTheme.colorScheme.onSurface),
        placeholder = { Text(placeholder, style = style.copy(color = MaterialTheme.colorScheme.outline)) },
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ImageAttachmentCard(att: Attachment, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 160.dp, height = 120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = att.uri,
            contentDescription = att.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            color = Color.Black.copy(alpha = 0.45f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clickable { onDelete() }
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "移除", tint = Color.White, modifier = Modifier.padding(6.dp))
        }
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
        ) {
            Text(
                text = att.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FileAttachmentRow(att: Attachment, onDelete: () -> Unit) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(att.uri), att.mime)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "打开 ${att.displayName}"))
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = att.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${att.mime} · ${Format.size(att.size)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "移除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
