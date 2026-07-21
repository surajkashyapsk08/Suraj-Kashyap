package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
  var studentId by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("login_screen_root")
  ) {
    // Elegant background layout
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .navigationBarsPadding()
        .statusBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Header Section
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(2.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_academy_logo),
            contentDescription = "Academy Logo",
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Suraj Sir Academy",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Text(
          text = "Student Portal Login",
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Input Form Section
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Welcome Back!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
          )

          Text(
            text = "Enter your academy credentials to access notes and quizzes.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
              .align(Alignment.Start)
              .padding(bottom = 16.dp)
          )

          if (errorMessage != null) {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.error,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("login_error_message")
            )
          }

          OutlinedTextField(
            value = studentId,
            onValueChange = {
              studentId = it
              errorMessage = null
            },
            label = { Text("Student ID or Email") },
            placeholder = { Text("e.g. SUR123") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("student_id_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Toggle password visibility")
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("password_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(24.dp))

          Button(
            onClick = {
              if (studentId.isBlank() || password.isBlank()) {
                errorMessage = "Please enter both Student ID and Password"
              } else {
                // Accepts any non-empty credentials for general accessibility
                onLoginSuccess()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("login_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Quick Demo Access Section (Great for immediate preview)
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "or speed-test our system instantly",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        ElevatedButton(
          onClick = {
            onLoginSuccess()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("demo_login_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
          )
        ) {
          Text("Demo Student Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "© 2026 Suraj Sir Academy. All Rights Reserved.",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
