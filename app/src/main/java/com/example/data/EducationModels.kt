package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class StudyChapter(
    val id: String,
    val title: String,
    val description: String,
    val readingNotes: List<NoteSection>,
    val quizQuestions: List<QuizQuestion>,
    var isCompleted: Boolean = false,
    var quizHighScore: Int? = null
)

data class NoteSection(
    val title: String,
    val content: String,
    val isFormulaOrHighlight: Boolean = false
)

data class Subject(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String, // "calculate", "science", "menu_book", "translate", "public"
    val accentColorHex: Long,
    val chapters: List<StudyChapter>
)

data class StudentProfile(
    val name: String,
    val gradeClass: String,
    val rollNo: String,
    val streakDays: Int,
    val totalStudyMinutes: Int,
    val badges: List<Badge>
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String // emoji or material icon
)

// In-Memory Database and state holder for tracking student learning progress in real-time!
object EducationRepository {
    
    private val _studentProfile = MutableStateFlow(
        StudentProfile(
            name = "Suraj Kashyap",
            gradeClass = "Class 10 - A",
            rollNo = "SEC10A-32",
            streakDays = 5,
            totalStudyMinutes = 340,
            badges = listOf(
                Badge("b1", "Early Bird", "Completed first quiz before 8 AM", "🌅"),
                Badge("b2", "Math Wizard", "Scored 100% in Mathematics quiz", "📐"),
                Badge("b3", "Science Champ", "Completed all Science chapters", "🧪")
            )
        )
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    init {
        loadInitialData()
    }

    fun updateProfileName(newName: String) {
        _studentProfile.value = _studentProfile.value.copy(name = newName)
    }

    fun addStudyMinutes(minutes: Int) {
        _studentProfile.value = _studentProfile.value.copy(
            totalStudyMinutes = _studentProfile.value.totalStudyMinutes + minutes
        )
    }

    fun updateQuizScore(subjectId: String, chapterId: String, scorePercentage: Int) {
        val updatedList = _subjects.value.map { subject ->
            if (subject.id == subjectId) {
                val updatedChapters = subject.chapters.map { chapter ->
                    if (chapter.id == chapterId) {
                        val currentHigh = chapter.quizHighScore ?: 0
                        chapter.copy(
                            isCompleted = true,
                            quizHighScore = maxOf(currentHigh, scorePercentage)
                        )
                    } else {
                        chapter
                    }
                }
                subject.copy(chapters = updatedChapters)
            } else {
                subject
            }
        }
        _subjects.value = updatedList

        // Check if we should unlock any badge or reward
        val completedCount = updatedList.flatMap { it.chapters }.count { it.isCompleted }
        val currentBadges = _studentProfile.value.badges.toMutableList()
        if (completedCount >= 3 && currentBadges.none { it.id == "b4" }) {
            currentBadges.add(Badge("b4", "Super Scholar", "Completed 3 or more chapters successfully", "🏆"))
        }
        _studentProfile.value = _studentProfile.value.copy(
            streakDays = _studentProfile.value.streakDays + 1,
            badges = currentBadges
        )
    }

    private fun loadInitialData() {
        _subjects.value = listOf(
            Subject(
                id = "math",
                name = "Mathematics",
                description = "Master the world of numbers, algebra, trigonometry, and core geometric formulas.",
                iconName = "calculate",
                accentColorHex = 0xFF4A90E2,
                chapters = listOf(
                    StudyChapter(
                        id = "trig",
                        title = "Introduction to Trigonometry",
                        description = "Understand trigonometric ratios, right-angled triangles, and standard identities.",
                        readingNotes = listOf(
                            NoteSection("1. Right-Angled Triangle Concepts", "Trigonometry studies the relationship between side lengths and angles of triangles. In a right-angled triangle, the primary sides are: Opposite (Perpendicular), Adjacent (Base), and Hypotenuse."),
                            NoteSection("2. Six Core Ratios", "The basic definitions for an angle θ are:\n• sine (sin θ) = Perpendicular / Hypotenuse\n• cosine (cos θ) = Base / Hypotenuse\n• tangent (tan θ) = Perpendicular / Base\n• cosecant (cosec θ) = Hypotenuse / Perpendicular\n• secant (sec θ) = Hypotenuse / Base\n• cotangent (cot θ) = Base / Perpendicular", isFormulaOrHighlight = true),
                            NoteSection("3. Pythagorean Trigonometric Identity", "A fundamental identity derived from the Pythagorean theorem:\nsin²θ + cos²θ = 1\n\nOther useful derivations:\n• 1 + tan²θ = sec²θ\n• 1 + cot²θ = cosec²θ", isFormulaOrHighlight = true),
                            NoteSection("4. Standard Angles Reference Table", "• sin(30°) = 1/2,  sin(45°) = 1/√2,  sin(60°) = √3/2,  sin(90°) = 1\n• cos(30°) = √3/2, cos(45°) = 1/√2,  cos(60°) = 1/2,  cos(90°) = 0\n• tan(30°) = 1/√3, tan(45°) = 1,  tan(60°) = √3,  tan(90°) = Undefined")
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "If sin θ = 3/5, what is the value of cos θ in a right-angled triangle?",
                                options = listOf("3/4", "4/5", "1/5", "5/4"),
                                correctAnswerIndex = 1,
                                explanation = "Using sin²θ + cos²θ = 1, we get cos²θ = 1 - (3/5)² = 1 - 9/25 = 16/25. Thus, cos θ = 4/5."
                            ),
                            QuizQuestion(
                                question = "What is the value of tan 45°?",
                                options = listOf("0", "1/√3", "1", "√3"),
                                correctAnswerIndex = 2,
                                explanation = "Since sin 45° = 1/√2 and cos 45° = 1/√2, their ratio tan 45° = sin 45° / cos 45° is equal to 1."
                            ),
                            QuizQuestion(
                                question = "Which of the following is equivalent to (1 - sin²θ) ?",
                                options = listOf("tan²θ", "cosec²θ", "sec²θ", "cos²θ"),
                                correctAnswerIndex = 3,
                                explanation = "By the standard identity sin²θ + cos²θ = 1, subtracting sin²θ from both sides yields cos²θ."
                            )
                        )
                    ),
                    StudyChapter(
                        id = "quad",
                        title = "Quadratic Equations",
                        description = "Solve quadratic equations using factoring, completing the square, and discriminant.",
                        readingNotes = listOf(
                            NoteSection("1. Standard Form", "A quadratic equation in variable x is an equation of the form:\nax² + bx + c = 0, where a, b, and c are real numbers, and a ≠ 0."),
                            NoteSection("2. Quadratic Formula", "The roots of a quadratic equation ax² + bx + c = 0 are given by:\nx = [-b ± √(b² - 4ac)] / (2a)", isFormulaOrHighlight = true),
                            NoteSection("3. The Discriminant & Nature of Roots", "The term D = b² - 4ac is called the discriminant. It determines the nature of the roots:\n• If D > 0: Two distinct real roots\n• If D = 0: Two equal real roots (perfect square)\n• If D < 0: No real roots (imaginary roots)", isFormulaOrHighlight = true)
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "What is the nature of roots for the equation x² - 4x + 4 = 0?",
                                options = listOf("Real and distinct", "Real and equal", "Imaginary/No real roots", "Undetermined"),
                                correctAnswerIndex = 1,
                                explanation = "The discriminant D = b² - 4ac = (-4)² - 4(1)(4) = 16 - 16 = 0. Since D = 0, the roots are real and equal."
                            ),
                            QuizQuestion(
                                question = "Solve x² - 5x + 6 = 0 for x.",
                                options = listOf("x = 1, 5", "x = 2, 3", "x = -2, -3", "x = 0, 6"),
                                correctAnswerIndex = 1,
                                explanation = "Factorizing: x² - 3x - 2x + 6 = 0 => x(x - 3) - 2(x - 3) = 0 => (x-2)(x-3) = 0. Thus, x = 2 and x = 3."
                            )
                        )
                    )
                )
            ),
            Subject(
                id = "science",
                name = "Science",
                description = "Explore physical laws, chemical compound families, biology, and environment ecosystems.",
                iconName = "science",
                accentColorHex = 0xFF50E3C2,
                chapters = listOf(
                    StudyChapter(
                        id = "light",
                        title = "Light: Reflection & Refraction",
                        description = "Learn laws of reflection, spherical mirrors, refraction index, and lens equations.",
                        readingNotes = listOf(
                            NoteSection("1. Laws of Reflection of Light", "1. The angle of incidence is equal to the angle of reflection (∠i = ∠r).\n2. The incident ray, the normal, and the reflected ray all lie in the same plane."),
                            NoteSection("2. Mirror Formula & Magnification", "The relation between focal length (f), image distance (v), and object distance (u) for spherical mirrors is:\n1/v + 1/u = 1/f\n\nLinear Magnification (m) = Image Height / Object Height = -v/u", isFormulaOrHighlight = true),
                            NoteSection("3. Refraction Index", "Refraction occurs because light travels at different speeds in different materials. Refractive index (n) of medium 2 with respect to medium 1:\nn = Speed of light in medium 1 / Speed of light in medium 2"),
                            NoteSection("4. Lens Formula & Power", "For thin spherical lenses:\n• Formula: 1/v - 1/u = 1/f\n• Power (P) = 1 / f (f in meters). The SI unit of power is Dioptre (D).", isFormulaOrHighlight = true)
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "Which formula represents the spherical mirror relation?",
                                options = listOf("1/v - 1/u = 1/f", "1/v + 1/u = 1/f", "v + u = f", "v * u = f²"),
                                correctAnswerIndex = 1,
                                explanation = "The mirror formula is 1/v + 1/u = 1/f. Conversely, the lens formula uses subtraction: 1/v - 1/u = 1/f."
                            ),
                            QuizQuestion(
                                question = "What is the SI unit of power of a lens?",
                                options = listOf("Meter", "Dioptre", "Watt", "Joule"),
                                correctAnswerIndex = 1,
                                explanation = "The power of a lens is measured in Dioptres (D), defined as the reciprocal of its focal length in meters."
                            )
                        )
                    ),
                    StudyChapter(
                        id = "carbon",
                        title = "Carbon & Its Compounds",
                        description = "Covalent bonding, carbon versatility, homologous series, and functional groups.",
                        readingNotes = listOf(
                            NoteSection("1. Covalent Bonding in Carbon", "Carbon has 4 valence electrons. To gain stability, it shares electrons with other atoms, forming covalent bonds. Sharing makes carbon exceptionally versatile."),
                            NoteSection("2. Saturated vs Unsaturated Hydrocarbons", "• Saturated (Alkanes): Single carbon-carbon bonds. General formula: C_n H_{2n+2}\n• Unsaturated (Alkenes/Alkynes): Double/triple bonds. Alkenes: C_n H_{2n}. Alkynes: C_n H_{2n-2}", isFormulaOrHighlight = true),
                            NoteSection("3. Homologous Series", "A family of organic compounds with the same functional group and similar chemical properties. Each successive member differs by a -CH₂- unit and 14 u atomic mass.")
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "What is the chemical formula for Propane?",
                                options = listOf("CH4", "C2H6", "C3H8", "C4H10"),
                                correctAnswerIndex = 2,
                                explanation = "Propane is an alkane with n=3. Using formula C_n H_{2n+2}, we get C3H(2*3+2) = C3H8."
                            ),
                            QuizQuestion(
                                question = "Which functional group is represented by -OH?",
                                options = listOf("Aldehyde", "Carboxylic Acid", "Ketone", "Alcohol"),
                                correctAnswerIndex = 3,
                                explanation = "-OH represents the Alcohol functional group. Aldehydes use -CHO, and Carboxylic Acids use -COOH."
                            )
                        )
                    )
                )
            ),
            Subject(
                id = "english",
                name = "English Grammar",
                description = "Enhance writing style, active usage of tenses, speech rules, and reading comprehension.",
                iconName = "menu_book",
                accentColorHex = 0xFFF5A623,
                chapters = listOf(
                    StudyChapter(
                        id = "tenses",
                        title = "Mastering English Tenses",
                        description = "Understand the 12 core tenses, helping structure sentence verbs accurately.",
                        readingNotes = listOf(
                            NoteSection("1. Present Tense", "• Simple Present: Habits or general truths (e.g., 'He writes notes').\n• Present Continuous: Ongoing action ('He is writing notes').\n• Present Perfect: Actions completed recently ('He has written notes').\n• Present Perfect Continuous: Action started in past, continuing now ('He has been writing')."),
                            NoteSection("2. Past Tense", "• Simple Past: Completed action in past ('He wrote notes').\n• Past Continuous: Action in progress at a past time ('He was writing notes').\n• Past Perfect: Action completed before another past action ('He had written notes')."),
                            NoteSection("3. Verb Auxiliary Checklist", "• Continuous -> use 'ing' form with am/is/are/was/were.\n• Perfect -> use Past Participle (V3) with has/have/had.", isFormulaOrHighlight = true)
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "Identify the tense: 'She has been working on this project since morning.'",
                                options = listOf("Present Continuous", "Present Perfect", "Present Perfect Continuous", "Past Perfect Continuous"),
                                correctAnswerIndex = 2,
                                explanation = "The structure 'has been + verb-ing' represents the Present Perfect Continuous tense."
                            ),
                            QuizQuestion(
                                question = "Choose the correct past participle (V3) of the verb 'Fly'.",
                                options = listOf("Flied", "Flew", "Flown", "Flying"),
                                correctAnswerIndex = 2,
                                explanation = "The conjugation of fly is: fly (base), flew (past), and flown (past participle)."
                            )
                        )
                    )
                )
            ),
            Subject(
                id = "hindi",
                name = "Hindi Grammar (हिंदी व्याकरण)",
                description = "हिंदी व्याकरण के महत्वपूर्ण नियम, कारक, संधि, समास और शब्द रचना सीखें।",
                iconName = "translate",
                accentColorHex = 0xFFD0021B,
                chapters = listOf(
                    StudyChapter(
                        id = "karak",
                        title = "कारक और उसके भेद",
                        description = "संज्ञा या सर्वनाम के जिस रूप से उसका संबंध वाक्य की क्रिया से जाना जाए, उसे कारक कहते हैं।",
                        readingNotes = listOf(
                            NoteSection("1. कारक की परिभाषा", "संज्ञा या सर्वनाम का वाक्य के अन्य पदों (विशेषकर क्रिया) से संबंध बताने वाले रूप को 'कारक' कहा जाता है। इसके चिन्हों को 'विभक्ति' या 'परसर्ग' कहते हैं।"),
                            NoteSection("2. कारक के 8 भेद (Vibhakti Chart)", "१. कर्ता कारक - विभक्ति: ने (कार्य करने वाला)\n२. कर्म कारक - विभक्ति: को (जिस पर क्रिया का फल पड़े)\n३. करण कारक - विभक्ति: से, के द्वारा (साधन)\n४. संप्रदान कारक - विभक्ति: को, के लिए (जिसके लिए कार्य हो)\n५. अपादान कारक - विभक्ति: से (अलग होने का भाव)\n६. संबंध कारक - विभक्ति: का, की, के, रा, री, रे\n७. अधिकरण कारक - विभक्ति: में, पर (आधार)\n८. संबोधन कारक - विभक्ति: हे! अरे!", isFormulaOrHighlight = true),
                            NoteSection("3. अपादान और करण में अंतर", "दोनों का चिन्ह 'से' है, परंतु:\n• करण में 'से' साधन के लिए होता है (जैसे: कलम से लिखना)।\n• अपादान में 'से' पृथकता/दूरी/डर के लिए होता है (जैसे: पेड़ से पत्ता गिरा)।")
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "'पेड़ से सेब गिरा।' वाक्य में 'से' किस कारक का सूचक है?",
                                options = listOf("करण कारक", "अपादान कारक", "कर्म कारक", "संबोधन कारक"),
                                correctAnswerIndex = 1,
                                explanation = "यहाँ सेब पेड़ से अलग हो रहा है। अलगाव (अलग होने) के अर्थ में 'से' विभक्ति का प्रयोग अपादान कारक में होता है।"
                            ),
                            QuizQuestion(
                                question = "कर्ता कारक का विभक्ति चिन्ह क्या है?",
                                options = listOf("को", "से", "ने", "के लिए"),
                                correctAnswerIndex = 2,
                                explanation = "कर्ता कारक का चिन्ह 'ने' होता है (जैसे: राम ने खाना खाया)।"
                            )
                        )
                    )
                )
            ),
            Subject(
                id = "social",
                name = "Social Science",
                description = "Dive into History, Civics, Geography and Indian Federal Governance structures.",
                iconName = "public",
                accentColorHex = 0xFF8B572A,
                chapters = listOf(
                    StudyChapter(
                        id = "nationalism",
                        title = "Nationalism in India",
                        description = "Understand Indian national struggles, Satyagraha, and critical independence movements.",
                        readingNotes = listOf(
                            NoteSection("1. Rowlatt Act & Jallianwala Bagh (1919)", "The Rowlatt Act authorized the British government to imprison political activists without trial. Opposing this, people gathered at Jallianwala Bagh on April 13, 1919, where General Dyer ordered troops to fire on the peaceful crowd, causing nationwide outrage."),
                            NoteSection("2. Non-Cooperation Movement (1920-1922)", "Launched by Mahatma Gandhi to demand Swaraj. It involved boycotting British goods, schools, and offices. Gandhi called off the movement in February 1922 due to the violent Chauri Chaura incident."),
                            NoteSection("3. Civil Disobedience & Salt March (1930)", "Gandhi started the movement with the historic Dandi Salt March on March 12, 1930. He walked 240 miles from Sabarmati Ashram to Dandi, breaking the monopoly salt law on April 6, 1930.", isFormulaOrHighlight = true)
                        ),
                        quizQuestions = listOf(
                            QuizQuestion(
                                question = "In which year did the Jallianwala Bagh massacre take place?",
                                options = listOf("1915", "1919", "1925", "1930"),
                                correctAnswerIndex = 1,
                                explanation = "The tragic Jallianwala Bagh massacre took place on April 13, 1919 in Amritsar, Punjab."
                            ),
                            QuizQuestion(
                                question = "Why did Mahatma Gandhi call off the Non-Cooperation Movement?",
                                options = listOf("Rowlatt Act passage", "Simon Commission arrival", "Chauri Chaura violent incident", "Salt Satyagraha commencement"),
                                correctAnswerIndex = 2,
                                explanation = "In February 1922, a violent clash at Chauri Chaura resulted in the burning of a police station, prompting Gandhi to call off the movement due to his commitment to absolute non-violence."
                            )
                        )
                    )
                )
            )
        )
    }
}
