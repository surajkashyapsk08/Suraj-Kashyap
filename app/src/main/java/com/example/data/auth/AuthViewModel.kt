package com.example.data.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _studentProfile = MutableStateFlow<StudentProfile?>(null)
    val studentProfile: StateFlow<StudentProfile?> = _studentProfile.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (auth == null) {
            _authState.value = AuthState.Unauthenticated
            return
        }
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                fetchStudentProfile(currentUser.uid)
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        if (auth == null) {
            _authState.value = AuthState.Error("Firebase is missing. Please add google-services.json")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    fetchStudentProfile(user.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Login failed. User not found.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed.")
            }
        }
    }

    fun signUp(fullName: String, email: String, password: String, studentClass: String) {
        if (auth == null || firestore == null) {
            _authState.value = AuthState.Error("Firebase is missing. Please add google-services.json")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    val profile = StudentProfile(
                        uid = user.uid,
                        fullName = fullName,
                        email = email,
                        studentClass = studentClass
                    )
                    
                    // Save to Firestore
                    firestore.collection("users").document(user.uid).set(profile).await()
                    
                    _studentProfile.value = profile
                    _authState.value = AuthState.Authenticated
                } ?: run {
                    _authState.value = AuthState.Error("Signup failed. User not found.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Signup failed.")
            }
        }
    }

    fun logout() {
        auth?.signOut()
        _studentProfile.value = null
        _authState.value = AuthState.Unauthenticated
    }

    private suspend fun fetchStudentProfile(uid: String) {
        if (firestore == null) {
            _authState.value = AuthState.Error("Firebase is missing.")
            return
        }
        try {
            val document = firestore.collection("users").document(uid).get().await()
            val profile = document.toObject(StudentProfile::class.java)
            if (profile != null) {
                _studentProfile.value = profile
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error("User profile not found in database.")
                auth?.signOut()
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching profile", e)
            _authState.value = AuthState.Error("Error fetching profile.")
            auth?.signOut()
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
