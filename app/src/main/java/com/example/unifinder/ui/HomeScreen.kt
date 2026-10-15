package com.example.unifinder.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unifinder.data.*
import com.example.unifinder.viewmodel.AuthViewModel
import com.example.unifinder.viewmodel.ItemViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale("th", "TH")).format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun kuChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    itemVm: ItemViewModel,
    authVm: AuthViewModel,
    onAdd: (String) -> Unit,
    onOpen: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val items by itemVm.items.collectAsState()
    val type by itemVm.type.collectAsState()
    val category by itemVm.category.collectAsState()
    val query by itemVm.query.collectAsState()
    val user by authVm.user.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "UniFinder", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "สวัสดี ${user?.name ?: ""}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "ออกจากระบบ") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAdd(type) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(if (type == ItemType.LOST) "ประกาศของหาย" else "ประกาศของที่เก็บได้") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = if (type == ItemType.LOST) 0 else 1,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = type == ItemType.LOST,
                    onClick = { itemVm.type.value = ItemType.LOST },
                    text = { Text("ของหาย") }
                )
                Tab(
                    selected = type == ItemType.FOUND,
                    onClick = { itemVm.type.value = ItemType.FOUND },
                    text = { Text("ของที่เก็บได้") }
                )
            }

            // ช่องค้นหา
            TextField(
                value = query, onValueChange = { itemVm.query.value = it },
                placeholder = { Text("ค้นหาชื่อ รายละเอียด สถานที่") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = category == "ALL",
                        onClick = { itemVm.category.value = "ALL" },
                        label = { Text("ทั้งหมด") },
                        colors = kuChipColors()
                    )
                }
                items(Categories.all) { c ->
                    FilterChip(
                        selected = category == c.key,
                        onClick = { itemVm.category.value = c.key },
                        label = { Text(c.label) },
                        leadingIcon = { Icon(c.icon, null, Modifier.size(18.dp)) },
                        colors = kuChipColors()
                    )
                }
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox, null, Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("ยังไม่มีประกาศ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(item) {onOpen(item.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(item: Item) {
    val done = item.status == ItemStatus.DONE
    val text = when {
        done && item.type == ItemType.LOST -> "พบแล้ว"
        done -> "ส่งคืนแล้ว"
        item.type == ItemType.LOST -> "กำลังตามหา"
        else -> "รอเจ้าของ"
    }
    val color = if (done) MaterialTheme.colorScheme.primary else Color(0xFFB26A00)
    val bg = if (done) MaterialTheme.colorScheme.primaryContainer else Color(0xFFFFF3E0)
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(
            text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    val cat = Categories.find(item.category)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.imagePath != null) {
                    AsyncImage(
                        model = File(item.imagePath), contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(cat.icon, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    item.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place, null, Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        item.location, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth, null, Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        formatDate(item.dateMillis), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(item)
            }
        }
    }
}