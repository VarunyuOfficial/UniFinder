package com.example.unifinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unifinder.data.*
import com.example.unifinder.viewmodel.AuthViewModel
import com.example.unifinder.viewmodel.ItemViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(itemId: Int, itemVm: ItemViewModel, authVm: AuthViewModel, onBack: () -> Unit) {
    val item by remember(itemId) { itemVm.observeItem(itemId) }.collectAsState(initial = null)
    val user by authVm.user.collectAsState()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "รายละเอียด", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "กลับ") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        val post = item
        if (post == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val cat = Categories.find(post.category)
        val isOwner = user?.id == post.ownerId
        val isLost = post.type == ItemType.LOST

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("ลบประกาศนี้?") },
                text = { Text("ลบแล้วไม่สามารถกู้คืนได้") },
                confirmButton = {
                    TextButton(onClick = { itemVm.delete(post, onBack) }) { Text("ลบ") }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("ยกเลิก") } }
            )
        }

        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(
                Modifier.fillMaxWidth().height(230.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (post.imagePath != null) {
                    AsyncImage(
                        model = File(post.imagePath), contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(cat.icon, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        post.title, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(post)
                }

                InfoRow(Icons.Default.Category, "ประเภท", cat.label)
                InfoRow(Icons.Default.Place, if (isLost) "สถานที่ที่หาย" else "สถานที่ที่พบ", post.location)
                InfoRow(
                    Icons.Default.CalendarMonth,
                    if (isLost) "วันที่หาย" else "วันที่พบ",
                    formatDate(post.dateMillis)
                )
                InfoRow(Icons.Default.Person, "ผู้ประกาศ", post.ownerName)
                InfoRow(Icons.Default.Phone, "ติดต่อ", post.contact)

                if (post.description.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("รายละเอียด", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(post.description)
                        }
                    }
                }

                if (isOwner) {
                    if (post.status == ItemStatus.OPEN) {
                        Button(
                            onClick = { itemVm.markDone(post.id) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isLost) "เปลี่ยนสถานะเป็น 'พบแล้ว'" else "เปลี่ยนสถานะเป็น 'ส่งคืนแล้ว'")
                        }
                    }
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ลบประกาศ")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp).size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}