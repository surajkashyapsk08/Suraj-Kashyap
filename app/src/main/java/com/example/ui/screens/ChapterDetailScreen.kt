package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EducationRepository
import com.example.data.QuizQuestion
import com.example.data.StudyChapter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
  subjectId: String,
  chapterId: String,
  onNavigateBack: () -> Unit
) {
  val subjects by EducationRepository.subjects.collectAsState()
  val subject = subjects.find { it.id == subjectId }
  val context = LocalContext.current

  // Attempt to load from offline storage first to provide offline access mechanism, 
  // fallback to live repository if not downloaded.
  val chapter = remember(chapterId, subject) {
    com.example.data.OfflineStorageManager.getDownloadedChapter(context, chapterId)
      ?: subject?.chapters?.find { it.id == chapterId }
  }

  if (subject == null || chapter == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Chapter syllabus details not found.")
    }
    return
  }

  val colors = getSubjectThemeColors(subjectId)
  val accentColor = colors.accent
  var selectedTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = Quiz

  // Log study time when entering details screen
  LaunchedEffect(chapterId) {
    EducationRepository.addStudyMinutes(10)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = chapter.title,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = subject.name,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("chapter_detail_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Default.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          var isDownloaded by remember(chapter.id) {
            mutableStateOf(com.example.data.OfflineStorageManager.isChapterDownloaded(context, chapter.id))
          }
          IconButton(
            onClick = {
              if (isDownloaded) {
                com.example.data.OfflineStorageManager.removeChapter(context, chapter.id)
                isDownloaded = false
                Toast.makeText(context, "Removed offline materials for '${chapter.title}'", Toast.LENGTH_SHORT).show()
              } else {
                com.example.data.OfflineStorageManager.downloadChapter(context, chapter)
                isDownloaded = true
                Toast.makeText(context, "Downloaded '${chapter.title}' for offline access", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.testTag("download_pdf_button")
          ) {
            Icon(
              imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
              contentDescription = if (isDownloaded) "Remove Offline Access" else "Download Offline",
              tint = accentColor
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
        .testTag("chapter_detail_root")
    ) {
      // Custom Material 3 Secondary Navigation Tab Bar
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = accentColor
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selectedTab == 0) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text("PDF Study Notes", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_notes")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selectedTab == 1) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text("Chapter Quiz", fontWeight = FontWeight.Bold)
            }
          },
          modifier = Modifier.testTag("tab_quiz")
        )
      }

      Box(modifier = Modifier.weight(1f)) {
        if (selectedTab == 0) {
          StudyNotesTab(chapter = chapter, accentColor = accentColor)
        } else {
          QuizTab(
            subjectId = subject.id,
            chapter = chapter,
            accentColor = accentColor
          )
        }
      }
    }
  }
}

@Composable
fun StudyNotesTab(chapter: StudyChapter, accentColor: Color) {
  var notesHighlighted by remember { mutableStateOf(false) }

  // Timer State
  var isTimerRunning by remember { mutableStateOf(false) }
  var studySeconds by remember { mutableStateOf(0) }

  LaunchedEffect(isTimerRunning) {
    if (isTimerRunning) {
      while (true) {
        kotlinx.coroutines.delay(1000L)
        studySeconds++
      }
    }
  }

  val formattedTime = remember(studySeconds) {
    val minutes = studySeconds / 60
    val seconds = studySeconds % 60
    "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("notes_container"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // PDF Warning / Educational Banner info
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "PDF Mode Info",
            tint = accentColor
          )
          Text(
            text = "Viewing High-Fidelity PDF Note booklet. Bookmark or highlights can be toggled.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Interactive Study Timer
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isTimerRunning) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Timer",
                tint = if (isTimerRunning) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Column {
              Text(
                text = "Focus Session",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = formattedTime,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTimerRunning) accentColor else MaterialTheme.colorScheme.onSurface
              )
            }
          }
          
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilledIconButton(
              onClick = { isTimerRunning = !isTimerRunning },
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isTimerRunning) MaterialTheme.colorScheme.surfaceVariant else accentColor,
                contentColor = if (isTimerRunning) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
              ),
              modifier = Modifier.testTag("timer_toggle_button")
            ) {
              Icon(
                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer"
              )
            }
            
            if (studySeconds > 0) {
              IconButton(
                onClick = { 
                  isTimerRunning = false
                  studySeconds = 0 
                },
                modifier = Modifier.testTag("timer_reset_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = "Reset Timer",
                  tint = MaterialTheme.colorScheme.error
                )
              }
            }
          }
        }
      }
    }

    // Toggle Highlight Buttons
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        FilterChip(
          selected = notesHighlighted,
          onClick = { notesHighlighted = !notesHighlighted },
          label = { Text("Highlight Formulas", fontSize = 12.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.BorderColor,
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          },
          modifier = Modifier.testTag("highlight_formulas_chip")
        )
      }
    }

    // List of Note Sections
    items(chapter.readingNotes) { section ->
      val isHighlighted = section.isFormulaOrHighlight && notesHighlighted
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = if (isHighlighted) 2.dp else 0.dp,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(16.dp)
          ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (section.isFormulaOrHighlight) {
            accentColor.copy(alpha = 0.05f)
          } else {
            MaterialTheme.colorScheme.surface
          }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (section.isFormulaOrHighlight) Icons.Default.Star else Icons.Default.MenuBook,
              contentDescription = null,
              tint = if (section.isFormulaOrHighlight) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = section.title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = if (section.isFormulaOrHighlight) accentColor else MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = section.content,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
            fontFamily = if (section.isFormulaOrHighlight) FontFamily.Serif else FontFamily.Default,
            fontWeight = if (section.isFormulaOrHighlight) FontWeight.Medium else FontWeight.Normal
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(48.dp))
    }
  }
}

@Composable
fun QuizTab(
  subjectId: String,
  chapter: StudyChapter,
  accentColor: Color
) {
  val questions = chapter.quizQuestions

  if (questions.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No quiz questions available for this chapter.")
    }
    return
  }

  var currentQuestionIndex by remember { mutableStateOf(0) }
  var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
  var hasSubmittedAnswer by remember { mutableStateOf(false) }
  var totalCorrectAnswers by remember { mutableStateOf(0) }
  var isQuizFinished by remember { mutableStateOf(false) }

  if (isQuizFinished) {
    // Render Results Screen
    val percentage = (totalCorrectAnswers * 100) / questions.size
    LaunchedEffect(percentage) {
      EducationRepository.updateQuizScore(subjectId, chapter.id, percentage)
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .testTag("quiz_results_view"),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (percentage >= 60) Icons.Default.EmojiEvents else Icons.Default.School,
            contentDescription = "Award",
            tint = accentColor,
            modifier = Modifier.size(56.dp)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = if (percentage >= 80) "Exceptional Performance!" else "Good Effort!",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "You scored $totalCorrectAnswers out of ${questions.size} correct.",
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
          colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "SCORE: $percentage%",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
          onClick = {
            currentQuestionIndex = 0
            selectedAnswerIndex = null
            hasSubmittedAnswer = false
            totalCorrectAnswers = 0
            isQuizFinished = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("quiz_retry_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Retake Chapter Quiz", fontWeight = FontWeight.Bold)
        }
      }
    }
    return
  }

  val activeQuestion = questions[currentQuestionIndex]

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("quiz_active_view"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Progress Indicators
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Success rate: $totalCorrectAnswers correct",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(CircleShape),
        color = accentColor,
        trackColor = accentColor.copy(alpha = 0.15f)
      )
    }

    // Active Question Box
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(vertical = 16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Text(
            text = activeQuestion.question,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
          )
        }

        // Options List (with touch target requirements and clear responses)
        items(activeQuestion.options.size) { index ->
          val optionText = activeQuestion.options[index]
          val isSelected = selectedAnswerIndex == index

          val optionCardColor = when {
            hasSubmittedAnswer && index == activeQuestion.correctAnswerIndex -> Color(0xFFE8F5E9) // Correct option green
            hasSubmittedAnswer && isSelected && index != activeQuestion.correctAnswerIndex -> Color(0xFFFFEBEE) // Wrong selection red
            isSelected -> accentColor.copy(alpha = 0.15f) // User selection prior to submission
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
          }

          val optionBorderColor = when {
            hasSubmittedAnswer && index == activeQuestion.correctAnswerIndex -> Color(0xFF2E7D32)
            hasSubmittedAnswer && isSelected && index != activeQuestion.correctAnswerIndex -> Color(0xFFC62828)
            isSelected -> accentColor
            else -> Color.Transparent
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 52.dp)
              .border(1.5.dp, optionBorderColor, RoundedCornerShape(12.dp))
              .clickable(enabled = !hasSubmittedAnswer) {
                selectedAnswerIndex = index
              }
              .testTag("quiz_option_$index"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = optionCardColor)
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = ('A'.code + index).toChar().toString(),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }

              Text(
                text = optionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Animated explanation card appearing once the question is evaluated!
        if (hasSubmittedAnswer) {
          item {
            val correct = selectedAnswerIndex == activeQuestion.correctAnswerIndex
            Card(
              modifier = Modifier.fillMaxWidth().animateContentSize(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (correct) Color(0xFFE8F5E9).copy(alpha = 0.4f) else Color(0xFFFFEBEE).copy(alpha = 0.4f)
              )
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = if (correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (correct) Color(0xFF2E7D32) else Color(0xFFC62828)
                  )
                  Text(
                    text = if (correct) "Correct Answer!" else "Incorrect Answer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (correct) Color(0xFF2E7D32) else Color(0xFFC62828)
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = activeQuestion.explanation,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  lineHeight = 18.sp
                )
              }
            }
          }
        }
      }
    }

    // Submit / Navigation Row at Bottom
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (!hasSubmittedAnswer) {
        Button(
          onClick = {
            if (selectedAnswerIndex != null) {
              hasSubmittedAnswer = true
              if (selectedAnswerIndex == activeQuestion.correctAnswerIndex) {
                totalCorrectAnswers++
              }
            }
          },
          enabled = selectedAnswerIndex != null,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("quiz_submit_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Evaluate Answer", fontWeight = FontWeight.Bold)
        }
      } else {
        Button(
          onClick = {
            if (currentQuestionIndex + 1 < questions.size) {
              currentQuestionIndex++
              selectedAnswerIndex = null
              hasSubmittedAnswer = false
            } else {
              isQuizFinished = true
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("quiz_next_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
          Text(
            text = if (currentQuestionIndex + 1 < questions.size) "Next Question" else "See Quiz Summary",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
