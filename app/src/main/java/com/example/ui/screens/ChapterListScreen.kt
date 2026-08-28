package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EducationRepository
import com.example.data.StudyChapter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
  subjectId: String,
  onNavigateBack: () -> Unit,
  onChapterSelect: (String) -> Unit
) {
  val subjects by EducationRepository.subjects.collectAsState()
  val subject = subjects.find { it.id == subjectId }

  if (subject == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Subject not found")
    }
    return
  }

  val completedCount = subject.chapters.count { it.isCompleted }
  val progress = if (subject.chapters.isNotEmpty()) completedCount.toFloat() / subject.chapters.size else 0f
  val colors = getSubjectThemeColors(subjectId)

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = subject.name, fontWeight = FontWeight.Bold, color = colors.text) },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("chapter_list_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Default.ArrowBack,
              contentDescription = "Back to Home",
              tint = colors.accent
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
        .testTag("chapter_list_root"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Subject Header Card (styled matching the subject color from Vibrant theme)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = colors.bg
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Course Description",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = colors.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = subject.description,
              fontSize = 14.sp,
              color = colors.text.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Syllabus completion stats
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Course Syllabus Completion",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
              )
              Text(
                text = "$completedCount / ${subject.chapters.size} Chapters Done",
                fontSize = 12.sp,
                color = colors.accent,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
              color = colors.accent,
              trackColor = colors.accent.copy(alpha = 0.15f)
            )
          }
        }
      }

      // Title Section
      item {
        Text(
          text = "Syllabus Chapter List",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
      }

      // Chapters List
      items(subject.chapters) { chapter ->
        ChapterItemCard(
          chapter = chapter,
          colors = colors,
          onClick = { onChapterSelect(chapter.id) }
        )
      }
    }
  }
}

@Composable
fun ChapterItemCard(
  chapter: StudyChapter,
  colors: SubjectThemeColors,
  onClick: () -> Unit
) {
  val context = LocalContext.current
  val isDownloaded by remember(chapter.id) {
    mutableStateOf(com.example.data.OfflineStorageManager.isChapterDownloaded(context, chapter.id))
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("chapter_card_${chapter.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Chapter indicator icon using accent colors
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(colors.accent),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      // Chapter Details
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = chapter.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          if (isDownloaded) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = "Available Offline",
              tint = colors.accent,
              modifier = Modifier.size(16.dp).padding(start = 4.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = chapter.description,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tags and statuses
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (chapter.isCompleted) {
            SuggestionChip(
              onClick = {},
              label = { Text("Read Notes", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.accent) },
              icon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(12.dp), tint = colors.accent) },
              colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = colors.bg
              ),
              border = null
            )
          }

          if (chapter.quizHighScore != null) {
            SuggestionChip(
              onClick = {},
              label = { Text("Quiz: ${chapter.quizHighScore}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
              colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ),
              border = null
            )
          } else {
            SuggestionChip(
              onClick = {},
              label = { Text("Quiz Pending", fontSize = 10.sp) },
              icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null, modifier = Modifier.size(12.dp)) },
              border = null
            )
          }
        }
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Open Chapter details",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
