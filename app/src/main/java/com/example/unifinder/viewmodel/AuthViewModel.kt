package com.example.unifinder.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unifinder.data.AppDatabase
import com.example.unifinder.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val userDao = AppDatabase.get(app).userDao()
    private val prefs = app.getSharedPreferences("session", Context.MODE_PRIVATE)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    init {
        viewModelScope.launch {
            val id = prefs.getInt("userId", -1)
            if (id != -1) _user.value = userDao.getById(id)
            _ready.value = true
        }
    }

    fun login(studentId: String, password: String) {
        if (studentId.isBlank() || password.isBlank()) {
            _message.value = "กรุณากรอกรหัสนิสิตและรหัสผ่าน"; return
        }
        viewModelScope.launch {
            val u = userDao.login(studentId.trim(), hash(password))
            if (u == null) _message.value = "ีรหัสนิสิตหรือรหัสผ่านไม่ถูกต้อง"
            else { saveSession(u); _user.value = u }
        }
    }

    fun register(studentId: String, name: String, password: String) {
        if (studentId.isBlank() || name.isBlank() || password.length < 4) {
            _message.value = "กรอกข้อมูลให้ครบถ้วน และรหัสผ่านอย่างน้อย 4 ตัว"; return
        }
        viewModelScope.launch {
            if (userDao.findByStudentId(studentId.trim()) != null) {
                _message.value = "รหัสนักศึกษานี้ถูกใช้แล้ว"; return@launch
            }
            val id = userDao.insert(User(studentId = studentId.trim(), name = name.trim(), passwordHash = hash(password)))
            val u = userDao.getById(id.toInt())
            if (u != null) { saveSession(u); _user.value = u }
        }
    }

    fun logout() {
        prefs.edit().remove("userId").apply()
        _user.value = null
    }

    fun clearMessage() { _message.value = null }

    private fun saveSession(u: User) = prefs.edit().putInt("userId", u.id).apply()

    private fun hash(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }
}