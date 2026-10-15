package com.example.unifinder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unifinder.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authVm: AuthViewModel, onLoggedIn: () -> Unit) {
    val user by authVm.user.collectAsState()
    val message by authVm.message.collectAsState()

    var isRegister by remember { mutableStateOf(false) }
    var studentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember {mutableStateOf("")}
    var showPass by remember { mutableStateOf(false) }

    LaunchedEffect(user) { if (user != null) onLoggedIn() }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(88.dp))

            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Icon(
                    Icons.Default.Search, null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(20.dp).size(40.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "UniFinder", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "ตามหาของหายในมหาวิทยาลัย",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    if (isRegister) "สมัครสมาชิก" else "เข้าสู่ระบบ",
                    fontSize = 20.sp, fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = studentId, onValueChange = { studentId = it },
                    label = { Text("รหัสนักศึกษา") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (isRegister) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("ชื่อ-นามสกุล") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("รหัสผ่าน") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null
                            )
                        }
                    },
                    visualTransformation =
                        if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        authVm.clearMessage()
                        if (isRegister) authVm.register(studentId, name, password)
                        else authVm.login(studentId, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(if (isRegister) "สมัครสมาชิก" else "เข้าสู่ระบบ", fontSize = 16.sp) }

                TextButton(
                    onClick = { isRegister = !isRegister; authVm.clearMessage() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (isRegister) "มีบัญชีแล้ว? เข้าสู่ระบบ" else "ยังไม่มีบัญชี? สมัครสมาชิก")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}