package com.example.unifinder.data

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.room.Entity
import androidx.room.PrimaryKey

/** data class 1: ผู้ใช้งาน **/
@Entity(tableName = "users")
data class User(
    @PrimaryKey(true) val id: Int = 0,
    val studentId: String,
    val name: String,
    val passwordHash: String
)

/** data class 2: ประกาศของหาย/ของที่เก็บได้ **/
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(true) val id: Int = 0,
    val type: String,                                    // (Item Lost or Found)
    val title: String,
    val description: String,
    val category: String,                                // key ของ Category
    val location: String,
    val dateMillis: Long,                                // วันที่หาย or วันที่พบ
    val imagePath: String? = null,                       // path รูปในเครื่อง
    val status: String = ItemStatus.OPEN,
    val ownerId: Int,
    val ownerName: String,
    val contact: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** data class 3: ประเภทสิ่งของ(สำหรับค้นหา/กรอง) **/
data class Category(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object ItemType {
    const val LOST = "LOST"
    const val FOUND = "FOUND"
}

object ItemStatus {
    const val OPEN = "OPEN" // ยังตามหาอยู่
    const val DONE = "DONE" // LOST: พบแล้ว / FOUND: ส่งคืนแล้ว
}

object Categories {
    val all = listOf(
        Category("WALLET", "กระเป๋าสตางค์", Icons.Default.AccountBalanceWallet),
        Category("CARD", "บัตร", Icons.Default.CreditCard),
        Category("PHONE", "โทรศัพท์/แท็บเล็ต", Icons.Default.PhoneAndroid),
        Category("KEYS", "กุญแจ", Icons.Default.Key),
        Category("BAG", "กระเป๋า", Icons.Default.ShoppingBag),
        Category("BOOK", "หนังสือ/อุปกรณ์เรียน", Icons.Default.Book),
        Category("OTHER", "อื่นๆ", Icons.Default.Category)
    )
    fun find(key: String) = all.firstOrNull { it.key == key } ?: all.last()
}