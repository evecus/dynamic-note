package com.note.dynamic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.note.dynamic.data.Note
import com.note.dynamic.data.NoteGroup
import com.note.dynamic.data.Palettes
import com.note.dynamic.ui.Format
import com.note.dynamic.ui.NotesViewModel
import com.note.dynamic.ui.screens.components.GroupDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    groupId: Long?,
    onOpenGroup: (Long) -> Unit = {},
    onOpenNote: (Long) -> Unit,
    onCreateNote: (Long?) -> Unit,
    onBack: () -> Unit = {}
) {
    val vm: NotesViewModel = viewModel(factory = NotesViewModel.Factory)
    val allGroups by vm.groups.collectAsStateWithLifecycle()
    var currentGroup by remember(groupId) { mutableStateOf<NoteGroup?>(null) }
    LaunchedEffect(groupId) {
        currentGroup = if (groupId != null) vm.getGroup(groupId) else null
    }

    val notesFlow = remember(groupId) {
        if (groupId != null) vm.notesInGroup(groupId) else vm.ungroupedNotes
    }
    val notes by notesFlow.collectAsStateWithLifecycle()
    val sortedNotes = remember(notes) {
        notes.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt })
    }

    var showGroupDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<NoteGroup?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val searchResults by vm.search(query).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentGroup?.name ?: "我的笔记",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val subtitle = currentGroup?.description?.takeIf { it.isNotBlank() }
                                ?: if (groupId == null) "记录每一个闪光的想法" else ""
                            if (subtitle.isNotEmpty()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (groupId != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch; query = "" }) {
                            Icon(
                                if (showSearch) Icons.Outlined.Close else Icons.Outlined.Search,
                                contentDescription = if (showSearch) "关闭搜索" else "搜索"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                AnimatedVisibility(visible = showSearch, enter = fadeIn(), exit = fadeOut()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜索笔记标题或内容…") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "清空")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onCreateNote(groupId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("新建笔记", fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            if (query.isNotBlank()) {
                item {
                    Text(
                        text = "搜索结果 · ${searchResults.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                    )
                }
                if (searchResults.isEmpty()) {
                    item { SearchEmptyHint() }
                } else {
                    items(searchResults, key = { it.id }) { note ->
                        NoteRow(
                            note = note,
                            onClick = { onOpenNote(note.id) },
                            onPin = { vm.togglePinned(note) },
                            onDelete = { vm.deleteNote(note) }
                        )
                    }
                }
                return@LazyColumn
            }

            if (groupId == null) {
                item {
                    SectionHeader(
                        title = "分组",
                        subtitle = "点击进入管理组内笔记",
                        actionText = "新建分组",
                        onAction = { editingGroup = null; showGroupDialog = true }
                    )
                }
                if (allGroups.isEmpty()) {
                    item {
                        EmptyGroupHint(onCreate = { editingGroup = null; showGroupDialog = true })
                    }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(allGroups, key = { it.id }) { group ->
                                val count by vm.noteCount(group.id).collectAsStateWithLifecycle(0)
                                GroupCard(
                                    group = group,
                                    noteCount = count,
                                    onClick = { onOpenGroup(group.id) },
                                    onEdit = { editingGroup = group; showGroupDialog = true },
                                    onDelete = { vm.deleteGroup(group) }
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item {
                SectionHeader(
                    title = if (groupId == null) "全部笔记" else "组内笔记",
                    subtitle = "${notes.size} 篇",
                    actionText = null,
                    onAction = {}
                )
            }
            if (sortedNotes.isEmpty()) {
                item { EmptyNotesHint(onCreate = { onCreateNote(groupId) }) }
            } else {
                items(sortedNotes, key = { it.id }) { note ->
                    NoteRow(
                        note = note,
                        onClick = { onOpenNote(note.id) },
                        onPin = { vm.togglePinned(note) },
                        onDelete = { vm.deleteNote(note) }
                    )
                }
            }
        }
    }

    if (showGroupDialog) {
        GroupDialog(
            initial = editingGroup,
            onDismiss = { showGroupDialog = false; editingGroup = null },
            onConfirm = { name, desc, color ->
                val target = editingGroup?.copy(name = name, description = desc, colorIndex = color)
                    ?: NoteGroup(name = name, description = desc, colorIndex = color)
                vm.saveGroup(target)
                showGroupDialog = false
                editingGroup = null
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionText != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = onAction
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(actionText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun EmptyGroupHint(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCreate() }
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.CreateNewFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("创建第一个分组", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("将相关笔记归类到一起", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EmptyNotesHint(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("还没有笔记", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text("点击右下角「新建笔记」开始记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SearchEmptyHint() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(80.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(38.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("没有找到相关笔记", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun GroupCard(
    group: NoteGroup,
    noteCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = Palettes.groupGradient(group.colorIndex)
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier
            .width(196.dp)
            .height(146.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.background(Brush.linearGradient(colors))) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.28f), modifier = Modifier.size(40.dp)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = !menuOpen }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "更多", tint = Color.White)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("编辑") }, onClick = { menuOpen = false; onEdit() })
                            DropdownMenuItem(text = { Text("删除") }, onClick = { menuOpen = false; onDelete() })
                        }
                    }
                }
                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val desc = group.description.takeIf { it.isNotBlank() } ?: "暂无描述"
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.3f)) {
                        Text(
                            text = "$noteCount 篇笔记",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    val accent by animateColorAsState(Palettes.noteAccent(note.colorIndex), label = "accent")
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            // 左侧色条，强调笔记颜色
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accent)
            )
            Spacer(Modifier.width(14.dp))
            Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f), modifier = Modifier.size(40.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (note.pinned) Icons.Rounded.PushPin else Icons.Outlined.Description,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.title.ifBlank { "无标题笔记" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (note.pinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.PushPin, contentDescription = "已置顶", tint = CoralForPin, modifier = Modifier.size(15.dp))
                    }
                }
                if (note.content.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = Format.preview(note.content),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = Format.time(note.updatedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
            }
            Box {
                IconButton(onClick = { menuOpen = !menuOpen }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "更多", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(if (note.pinned) "取消置顶" else "置顶") },
                        onClick = { menuOpen = false; onPin() },
                        leadingIcon = { Icon(Icons.Outlined.PushPin, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = { menuOpen = false; onDelete() },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

private val CoralForPin = Color(0xFFFF7E5F)
