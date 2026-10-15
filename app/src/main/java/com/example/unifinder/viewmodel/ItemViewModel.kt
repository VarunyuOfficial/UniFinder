package com.example.unifinder.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unifinder.data.AppDatabase
import com.example.unifinder.data.Item
import com.example.unifinder.data.ItemStatus
import com.example.unifinder.data.ItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModel(private val app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).itemDao()

    val type = MutableStateFlow(ItemType.LOST)
    val category = MutableStateFlow("ALL")
    val query = MutableStateFlow("")

    val items: StateFlow<List<Item>> =
        combine(dao.getAll(), type, category, query) { all, t, c, q ->
            val key = q.trim()
            all.filter { item ->
                item.type == t &&                                  // แท็บ ของหาย / ของที่เก็บได้
                        (c == "ALL" || item.category == c) &&              // หมวดหมู่
                        (key.isEmpty() ||                                  // คำค้นหา
                                item.title.contains(key, ignoreCase = true) ||
                                item.description.contains(key, ignoreCase = true) ||
                                item.location.contains(key, ignoreCase = true))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeItem(id: Int): Flow<Item?> = dao.observe(id)

    fun addItem(item: Item, imageUri: Uri?, onDone: () -> Unit) {
        viewModelScope.launch {
            val path = if (imageUri != null) withContext(Dispatchers.IO) { copyImage(imageUri) } else null
            dao.insert(item.copy(imagePath = path))
            onDone()
        }
    }

    fun markDone(id: Int) {
        viewModelScope.launch { dao.updateStatus(id, ItemStatus.DONE) }
    }

    fun delete(item: Item, onDone: () -> Unit) {
        viewModelScope.launch {
            dao.delete(item)
            item.imagePath?.let { withContext(Dispatchers.IO) { File(it).delete() } }
            onDone()
        }
    }

    // คัดลอกรูปที่เลือกเก็บไว้ในข้อมูลแอป เพื่อให้รูปไม่หาย
    private fun copyImage(uri: Uri): String? = try {
        val file = File(app.filesDir, "img_${System.currentTimeMillis()}.jpg")
        app.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        file.absolutePath
    } catch (e: Exception) { null }
}