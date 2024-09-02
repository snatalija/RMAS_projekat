import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AllDanceClubsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _clubs = MutableStateFlow<List<Club>>(emptyList())
    val clubs: StateFlow<List<Club>> = _clubs.asStateFlow()

    private val _authorNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val authorNames: StateFlow<Map<String, String>> = _authorNames.asStateFlow()

    private val authorCache = mutableMapOf<String, String>() // Local cache for author names

    init {
        loadClubs()
    }

    private fun loadClubs() {
        viewModelScope.launch {
            try {
                val clubsSnapshot = firestore.collection("dance_clubs").get().await()
                val clubList = clubsSnapshot.documents.mapNotNull { document ->
                    document.toObject(Club::class.java)?.copy(ownerId = document.id) // Ensure userId is included
                }
                _clubs.value = clubList

                // Fetch author names
                val authorIds = clubList.map { it.ownerId }.distinct()
                val authorNameMap = mutableMapOf<String, String>()
                authorIds.forEach { userId ->
                    // Use cached data if available
                    if (authorCache.containsKey(userId)) {
                        authorNameMap[userId] = authorCache[userId]!!
                    } else {
                        viewModelScope.launch {
                            val userName = getUserName(userId)
                            authorCache[userId] = userName // Cache the result
                            authorNameMap[userId] = userName
                            _authorNames.value = authorNameMap // Update authorNames
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun getUserName(userId: String): String {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val firstName = userDoc.getString("firstName") ?: ""
            val lastName = userDoc.getString("lastName") ?: ""
            "$firstName $lastName".trim()
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
