package com.example.simplecleanarchitecture.users.ui.useredit

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.simplecleanarchitecture.MainRouter
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.databinding.UserEditFragmentBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.FileNotFoundException
import java.io.InputStream


class UserEditFragment : Fragment() {

    private lateinit var binding: UserEditFragmentBinding

    private val viewModel: UserEditViewModel by stateViewModel(state = { Bundle.EMPTY }) {
        parametersOf(arguments?.getString(USER_ID_KEY) ?: "")
    }

    private val router: MainRouter by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.user_edit_fragment, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            AlertDialog.Builder(requireContext())
                .setMessage(it)
                .setPositiveButton(R.string.dialog_button_close, { _, _ -> })
                .create()
                .show()
        })
        viewModel.preloader.observe(viewLifecycleOwner, Observer {
            if (it) {
                hideKeyboard()
            }
        })
        viewModel.screenRouting.observe(viewLifecycleOwner) {
            router.execute(it)
        }
        viewModel.avatar.observe(viewLifecycleOwner) {
            it?.let {
                binding.avatarImage.setImageBitmap(
                    BitmapFactory.decodeByteArray(it, 0, it.size, BitmapFactory.Options())
                )
            } ?: run {
                binding.avatarImage.setImageResource(R.drawable.user_avatar_ico)
            }
        }
        viewModel.idScan.observe(viewLifecycleOwner) {
            it?.let {
                binding.idScanImage.setImageBitmap(
                    BitmapFactory.decodeByteArray(it, 0, it.size, BitmapFactory.Options())
                )
            } ?: run {
                binding.idScanImage.setImageResource(R.drawable.user_id_scan_ico)
            }
        }
        binding.avatarImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                setType("*/*")
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpg", "image/jpeg"))
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, getString(R.string.image_chooser_title)), AVATAR_REQUEST_ID)
        }
        binding.idScanImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpg", "image/jpeg"))
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, getString(R.string.image_chooser_title)), ID_SCAN_REQUEST_ID)
        }
        viewModel.loadDetails()
    }

    private fun hideKeyboard() {
        view?.rootView?.windowToken?.let { token ->
            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(token, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AVATAR_REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { url ->
                viewModel.addAvatar(url.toString())
            }
        } else if (requestCode == ID_SCAN_REQUEST_ID && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { url ->
                viewModel.addIdScan(url.toString())
            }
        }
    }

    companion object {

        private const val AVATAR_REQUEST_ID = 1001
        private const val ID_SCAN_REQUEST_ID = 1002

        private const val USER_ID_KEY = "USER_ID_KEY"

        fun newInstance(id: String? = null) = UserEditFragment().apply {
            arguments = Bundle().apply {
                putString(USER_ID_KEY, id)
            }
        }
    }

}