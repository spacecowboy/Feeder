# complete code
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class RenameLabelDialog(private val onRenameLabel: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.filters = arrayOf(InputFilter.LengthFilter(50))
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onRenameLabel(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        builder.setView(editText)
        builder.setPositiveButton("Rename") { _, _ -> onRenameLabel(editText.text.toString()) }
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }

        return builder.create()
    }
}