package com.example.unifinder.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE studentId = :studentId LIMIT 1")
    suspend fun findByStudentId(studentId: String): User?

    @Query("SELECT * FROM users WHERE studentId = :studentId AND passwordHash = :hash LIMIT 1")
    suspend fun login(studentId: String, hash: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): User?
}

@Dao
interface ItemDao {
    // ค้นหาตามประเภทประกาศ + ประเภทสิ่งของ + คำค้นหา (ชื่อ/รายละเอียด/สถานที่)
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun observe(id: Int): Flow<Item?>

    @Insert
    suspend fun insert(item: Item)

    @Query("UPDATE items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Delete
    suspend fun delete(item: Item)
}