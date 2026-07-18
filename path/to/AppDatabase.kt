# complete code
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Feed::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedsDao(): FeedsDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "database-name"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Create the table for the label name
                        db.execSQL("CREATE TABLE label_name (id INTEGER PRIMARY KEY, label_name TEXT)")
                    }
                }).build()
            }
            return instance!!
        }
    }
}