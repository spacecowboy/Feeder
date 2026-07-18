# complete code
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val url: String,
    val label: String
) {

    fun renameLabel(newLabelName: String) {
        // Update the label name in the database
        val db = AppDatabase.getInstance()
        val feedsDao = db.feedsDao()
        feedsDao.updateLabelName(newLabelName)
    }
}