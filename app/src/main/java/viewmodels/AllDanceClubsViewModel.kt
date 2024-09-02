import android.util.Log
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

    private val _ownerNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val ownerNames: StateFlow<Map<String, String>> = _ownerNames.asStateFlow()
    fun loadClubs() {
        viewModelScope.launch {
            try {
                val clubsSnapshot = firestore.collection("dance_clubs").get().await()
                val clubList = clubsSnapshot.documents.mapNotNull { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val danceType = document.getString("danceType") ?: ""
                    val workingHours = document.getString("workingHours") ?: ""
                    val userId = document.getString("userId") ?: ""
                    val hasReviewed = document.getBoolean("hasReviewed") // Retrieve as Boolean?
                    val averageRating = document.getDouble("averageRating")?.toFloat() ?: 0f

                    Club(id, name, danceType, workingHours, userId, hasReviewed, averageRating)
                }
                _clubs.value = clubList

                // Fetch owner names for each club
                val ownerNamesMap = mutableMapOf<String, String>()
                for (club in clubList) {
                    val ownerId = club.userId ?: continue
                    val ownerName = getUserName(ownerId)
                    ownerNamesMap[club.id] = ownerName
                }
                _ownerNames.value = ownerNamesMap

            } catch (e: Exception) {
                Log.e("AllDanceClubsViewModel", "Error loading clubs", e)
            }
        }
    }

    private suspend fun getUserName(userId: String): String {
        return try {
            Log.d("nikola", userId)
            val userDoc = firestore.collection("users").document(userId).get().await()
            val firstName = userDoc.getString("firstName") ?: ""
            val lastName = userDoc.getString("lastName") ?: ""
            Log.d("nikola", firstName)
            Log.d("nikola", lastName)
            "$firstName $lastName".trim()
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
