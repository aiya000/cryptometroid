package org.cryptomator.presentation.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.cryptomator.presentation.R

class GenerateAllThumbnailsDialog : DialogFragment() {

	interface Callback {
		fun onGenerateAllThumbnailsConfirmed()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val callback = requireActivity() as Callback
		
		return AlertDialog.Builder(requireActivity())
			.setTitle(getString(R.string.dialog_generate_all_thumbnails_title))
			.setMessage(getString(R.string.dialog_generate_all_thumbnails_message))
			.setPositiveButton(getString(R.string.dialog_generate_all_thumbnails_positive_button)) { _: DialogInterface, _: Int -> 
				callback.onGenerateAllThumbnailsConfirmed() 
			}
			.setNegativeButton(getString(R.string.dialog_generate_all_thumbnails_negative_button)) { _: DialogInterface, _: Int -> }
			.create()
	}

	companion object {
		fun newInstance(): DialogFragment {
			return GenerateAllThumbnailsDialog()
		}
	}
}