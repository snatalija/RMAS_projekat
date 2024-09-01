import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

object FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(fileUri: Uri, path: String): String {
        val storageRef = storage.reference.child(path)
        val uploadTask = storageRef.putFile(fileUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}
