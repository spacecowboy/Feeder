# complete code
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FeedsDao {

    @Insert
    fun insertFeed(feed: Feed)

    @Update
    fun updateFeed(feed: Feed)

    @Query("SELECT * FROM feeds")
    fun getFeeds(): List<Feed>

    @Query("SELECT label_name FROM label_name WHERE id = :id")
    fun getLabelName(id: Int): String?

    @Update
    fun updateLabelName(newLabelName: String)
}