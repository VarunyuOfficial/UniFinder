package com.example.unifinder.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.unifinder.data.*
import com.example.unifinder.viewmodel.AuthViewModel
import com.example.unifinder.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    type: String,
    itemVm: ItemViewModel,
    authVm: AuthViewModel,
    onBack: () -> Unit
) {
    val user by authVm.user.collectAsState()
    val isLost = type == ItemType.LOST

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Categories.all.first().key) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDate by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }

    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateMillis = it }
                    showDate = false
                }) { Text("ตกลง") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("ยกเลิก") } }
        ) { DatePicker(state = state) }
    }

    val fieldShape = RoundedCornerShape(14.dp)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isLost) "ประกาศของหาย" else "ประกาศของที่เก็บได้",
                        fontWeight = FontWeight.SemiBold,
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
        Column(
            Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // กล่องเลือกรูป
            Box(
                Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate, null, Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("แตะเพื่อเพิ่มรูปภาพสิ่งของ", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("ชื่อสิ่งของ เช่น กระเป๋าสตางค์สีดำ") },
                singleLine = true, shape = fieldShape, modifier = Modifier.fillMaxWidth()
            )

            Text("ประเภทสิ่งของ", fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Categories.all) { c ->
                    FilterChip(
                        selected = category == c.key, onClick = { category = c.key },
                        label = { Text(c.label) },
                        leadingIcon = { Icon(c.icon, null, Modifier.size(18.dp)) },
                        colors = kuChipColors()
                    )
                }
            }

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("รายละเอียด (สี ยี่ห้อ ตำหนิ ฯลฯ)") },
                minLines = 3, shape = fieldShape, modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text(if (isLost) "สถานที่ที่ทำหาย" else "สถานที่ที่พบ") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                singleLine = true, shape = fieldShape, modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showDate = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = fieldShape
            ) {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(Modifier.width(8.dp))
                Text((if (isLost) "วันที่หาย: " else "วันที่พบ: ") + formatDate(dateMillis))
            }

            OutlinedTextField(
                value = contact, onValueChange = { contact = it },
                label = { Text("ช่องทางติดต่อ (เบอร์โทร / LINE)") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                singleLine = true, shape = fieldShape, modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val u = user ?: return@Button
                    if (title.isBlank() || location.isBlank() || contact.isBlank()) {
                        error = "กรุณากรอกชื่อสิ่งของ สถานที่ และช่องทางติดต่อ"
                        return@Button
                    }
                    itemVm.addItem(
                        Item(
                            type = type, title = title.trim(), description = description.trim(),
                            category = category, location = location.trim(), dateMillis = dateMillis,
                            ownerId = u.id, ownerName = u.name, contact = contact.trim()
                        ),
                        imageUri,
                        onDone = onBack
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = fieldShape
            ) { Text("ลงประกาศ") }
            Spacer(Modifier.height(16.dp))
        }
    }
}