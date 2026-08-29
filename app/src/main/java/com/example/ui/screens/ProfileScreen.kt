package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.EducationRepository
import com.example.data.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  authViewModel: AuthViewModel,
  onNavigateBack: () -> Unit,
  onLogout: () -> Unit
) {
  val profile by authViewModel.studentProfile.collectAsState()
  val demoProfile by EducationRepository.studentProfile.collectAsState()
  val subjects by EducationRepository.subjects.collectAsState()
  val context = LocalContext.current

  val completedCount = subjects.flatMap { it.chapters }.count { it.isCompleted }

  var isEditingName by remember { mutableStateOf(false) }
  var editedName by remember { mutableStateOf(profile?.fullName ?: "") }

  var notificationsEnabled by remember { mutableStateOf(true) }
  var offlineModeEnabled by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Student Profile", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("profile_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Default.ArrowBack,
              contentDescription = "Back to Home"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              Toast.makeText(context, "Syllabus report cards synced!", Toast.LENGTH_SHORT).show()
            }
          ) {
            Icon(Icons.Default.CloudSync, contentDescription = "Sync Report Card")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
        .testTag("profile_root"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Primary Profile Identification Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
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
            Box(
              modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(2.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_academy_logo),
                contentDescription = "Student Avatar",
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape),
                contentScale = ContentScale.Crop
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Name editing input field!
            if (isEditingName) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                OutlinedTextField(
                  value = editedName,
                  onValueChange = { editedName = it },
                  label = { Text("Student Name") },
                  singleLine = true,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("profile_name_input"),
                  shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                  onClick = {
                    if (editedName.isNotBlank()) {
                      EducationRepository.updateProfileName(editedName)
                      isEditingName = false
                    }
                  },
                  modifier = Modifier.testTag("profile_name_save_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save name",
                    tint = Color(0xFF2E7D32)
                  )
                }
              }
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { isEditingName = true }
              ) {
                Text(
                  text = profile?.fullName ?: "Student",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag("profile_student_name")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Name",
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = profile?.studentClass ?: "Class 10",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = "Roll: ${profile?.uid?.take(4) ?: "N/A"} • ${profile?.studentClass ?: "Class 10"}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }
      }

      // Live Learning Analytics Stats Grid
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            modifier = Modifier.weight(1f),
            title = "Streak Days",
            value = "${profile?.streakDays ?: 0} Days",
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFE64A19)
          )
          StatCard(
            modifier = Modifier.weight(1f),
            title = "Study Time",
            value = "${profile?.studyMinutes ?: 0} Mins",
            icon = Icons.Default.Timer,
            iconColor = Color(0xFF1976D2)
          )
          StatCard(
            modifier = Modifier.weight(1f),
            title = "Syllabus",
            value = "$completedCount Chapters",
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF388E3C)
          )
        }
      }

      // Academic Progress Section
      item {
        val completedOrQuizChapters = subjects.flatMap { it.chapters }.filter { it.isCompleted || it.quizHighScore != null }
        if (completedOrQuizChapters.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Academic Progress",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            for (chapter in completedOrQuizChapters) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surface
                )
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = chapter.title,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (chapter.isCompleted) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = "Completed",
                          tint = Color(0xFF388E3C),
                          modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = "Completed",
                          fontSize = 11.sp,
                          color = Color(0xFF388E3C),
                          fontWeight = FontWeight.Medium
                        )
                      }
                    } else {
                      Text(
                        text = "In Progress",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                  if (chapter.quizHighScore != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                      Text(
                        text = "${chapter.quizHighScore}%",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                      )
                      Text(
                        text = "Score",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                      )
                    }
                  }
                }
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
          }
        }
      }

      // Achievement Badges Earned Section
      item {
        Text(
          text = "Unlocked Academy Badges (${demoProfile.badges.size})",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      item {
        Column(
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (badge in demoProfile.badges) {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("badge_item_${badge.id}"),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
              )
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = badge.icon, fontSize = 24.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = badge.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = badge.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Icon(
                  imageVector = Icons.Default.WorkspacePremium,
                  contentDescription = "Badge Unlocked",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }

      // Supportive System Preferences / Switches
      item {
        Text(
          text = "App Settings",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                Column {
                  Text("Push Notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                  Text("Get daily homework reminders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
                modifier = Modifier.testTag("notifications_switch")
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Icon(Icons.Default.OfflinePin, null, tint = MaterialTheme.colorScheme.primary)
                Column {
                  Text("Offline Study Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                  Text("Cache notes and active quizzes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = offlineModeEnabled,
                onCheckedChange = { offlineModeEnabled = it },
                modifier = Modifier.testTag("offline_switch")
              )
            }
          }
        }
      }

      // Logout button
      item {
        Button(
          onClick = onLogout,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("logout_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          )
        ) {
          Icon(Icons.Default.Logout, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Logout from Portal", fontWeight = FontWeight.Bold)
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun StatCard(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  icon: ImageVector,
  iconColor: Color
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title,
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}
