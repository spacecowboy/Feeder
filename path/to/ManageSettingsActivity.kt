# complete code
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class ManageSettingsActivity : AppCompatActivity() {

    private lateinit var labelRenameButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_settings)

        labelRenameButton = findViewById(R.id.label_rename_button)
        labelRenameButton.setOnClickListener {
            val renameLabelDialog = RenameLabelDialog { newLabelName ->
                // Update the label name in the database
                val db = AppDatabase.getInstance(this)
                val feedsDao = db.feedsDao()
                feedsDao.updateLabelName(newLabelName)
                labelRenameButton.text = newLabelName
            }
            renameLabelDialog.show(supportFragmentManager, "RenameLabelDialog")
        }
    }
}