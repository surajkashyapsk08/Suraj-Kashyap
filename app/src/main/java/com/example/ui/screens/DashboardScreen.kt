package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.EducationRepository
import com.example.data.Subject
import com.example.data.auth.AuthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  authViewModel: AuthViewModel,
  onSubjectSelect: (String) -> Unit,
  onNavigateToProfile: () -> Unit
) {
  val subjects by EducationRepository.subjects.collectAsState()
  val profile by authViewModel.studentProfile.collectAsState()
  var searchQuery by remember { mutableStateOf("") }

  val filteredSubjects = remember(searchQuery, subjects) {
    if (searchQuery.isBlank()) subjects else {
      subjects.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
          it.description.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Scaffold { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
        .testTag("dashboard_root"),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. Welcome Header (from Design HTML)
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Welcome back,",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.5.sp
            )
            Text(
              text = profile?.fullName ?: "Student",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground,
              letterSpacing = (-0.5).sp
            )
          }
          // Profile Avatar Circle styled like the Design HTML: bg-[#eaddff] border-2 border-[#6750a4] active:scale-95 transition-transform shadow-sm
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onNavigateToProfile() }
              .padding(2.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_academy_logo),
              contentDescription = "Profile",
              modifier = Modifier.fillMaxSize().clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          }
        }
      }

      // 2. Overall Learning Progress Section (from Design HTML)
      item {
        val totalChaptersAll = subjects.flatMap { it.chapters }.size
        val completedChaptersAll = subjects.flatMap { it.chapters }.count { it.isCompleted }
        val overallPercentage = if (totalChaptersAll > 0) {
          (completedChaptersAll.toFloat() / totalChaptersAll * 100).toInt()
        } else {
          72 // Fallback to 72% as in the Design HTML
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Overall Learning Progress",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
              )
              Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "$overallPercentage%",
                  fontSize = 36.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  letterSpacing = (-1).sp
                )
                Text(
                  text = "+12% from last week",
                  fontSize = 11.sp,
                  color = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.padding(bottom = 4.dp)
                )
              }
              // Progress Bar (from Design HTML: w-full bg-white/20 h-2 rounded-full mt-2 overflow-hidden)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp)
                  .height(8.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.2f))
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(overallPercentage.toFloat() / 100f)
                    .clip(CircleShape)
                    .background(Color.White)
                )
              }
            }
          }
        }
      }

      // 3. Streak Card (from Design HTML)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
          )
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(text = "🔥", fontSize = 18.sp)
              Column {
                Text(
                  text = "Your Study Streak",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                  text = "Keep learning daily to grow your knowledge!",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
              }
            }
            Text(
              text = "${profile?.streakDays ?: 0} Days",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // Notice Board / Message from Suraj Sir
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
            ) {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Notice",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                  .align(Alignment.Center)
                  .size(28.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Suraj Sir's Notice Board",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "Mathematics Doubt-Clearing interactive session starts tomorrow at 5:00 PM. Keep your notes ready!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search subjects, chapters, or concepts...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_search_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary
          )
        )
      }

      // Subjects Grid Title
      item {
        Text(
          text = "Select Course Subject",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      // Responsive Grid of Subjects (Grid rendered inside LazyColumn using custom grid layouts to ensure scrolling fluidity)
      item {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val chunks = filteredSubjects.chunked(2)
          for (chunk in chunks) {
            if (chunk.size == 1) {
              // Full-width (equivalent to col-span-2)
              SubjectCard(
                subject = chunk[0],
                onClick = { onSubjectSelect(chunk[0].id) }
              )
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                for (subject in chunk) {
                  Box(modifier = Modifier.weight(1f)) {
                    SubjectCard(
                      subject = subject,
                      onClick = { onSubjectSelect(subject.id) }
                    )
                  }
                }
              }
            }
          }

          if (filteredSubjects.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.SearchOff,
                  contentDescription = "Not Found",
                  modifier = Modifier.size(48.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "No subjects found for \"$searchQuery\"",
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SubjectCard(subject: Subject, onClick: () -> Unit) {
  val icon = when (subject.iconName) {
    "calculate" -> Icons.Default.Calculate
    "science" -> Icons.Default.Science
    "menu_book" -> Icons.Default.MenuBook
    "translate" -> Icons.Default.Translate
    "public" -> Icons.Default.Public
    else -> Icons.Default.Book
  }

  val totalChapters = subject.chapters.size
  val completedChapters = subject.chapters.count { it.isCompleted }
  val progress = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f
  val colors = getSubjectThemeColors(subject.id)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("subject_card_${subject.id}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = colors.bg
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.accent)
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
              .align(Alignment.Center)
              .size(22.dp)
          )
        }

        if (progress == 1f) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Completed",
            tint = colors.accent,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = subject.name,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = colors.text
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subject.description,
        fontSize = 11.sp,
        color = colors.text.copy(alpha = 0.75f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 15.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Progress Tracker inside Card
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (progress == 1f) "Completed" else "$completedChapters/$totalChapters Chapters",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
          )
          Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text.copy(alpha = 0.8f)
          )
        }
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
          color = colors.accent,
          trackColor = colors.accent.copy(alpha = 0.15f)
        )
      }
    }
  }
}
