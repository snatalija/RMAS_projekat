import com.google.firebase.firestore.DocumentId

data class Club(
    @DocumentId val id: String = "",
    val name: String = "",
    val danceType: String = "",
    val workingHours: String = "",
    val userId: String = "",
    val hasReviewed: Boolean = false,
    val averageRating: Float = 0f
)