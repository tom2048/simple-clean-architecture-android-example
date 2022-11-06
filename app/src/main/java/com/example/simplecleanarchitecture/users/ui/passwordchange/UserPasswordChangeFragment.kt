package com.example.simplecleanarchitecture.users.ui.passwordchange

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.simplecleanarchitecture.MainRouter
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.databinding.UserPasswordChangeFragmentBinding
import com.github.terrakok.cicerone.Back
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.core.parameter.parametersOf

class UserPasswordChangeFragment : Fragment() {

    private val viewModel: UserPasswordChangeViewModel by stateViewModel(state = { Bundle.EMPTY }) {
        parametersOf(arguments?.getString(USER_ID_KEY) ?: "")
    }

    private val routing: MainRouter by inject()

    private lateinit var binding: UserPasswordChangeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.user_password_change_fragment, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.message.observe(viewLifecycleOwner, { message ->
            AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_close, DialogInterface.OnClickListener { _, _ -> })
                .create()
                .show()

        })
        viewModel.preloader.observe(viewLifecycleOwner, {
            if (it) {
                hideKeyboard()          // TODO: could be hidden based on separate livedata
            }
        })
        viewModel.routing.observe(viewLifecycleOwner, { routingCommand ->
            routing.execute(routingCommand)
        })
        if (arguments?.getString(USER_ID_KEY).isNullOrEmpty()) {
            routing.execute(Back())
        }
    }

    private fun hideKeyboard() {
        view?.rootView?.windowToken?.let { token ->
            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(token, 0)
        }
    }

    companion object {

        private const val USER_ID_KEY = "USER_ID_KEY"

        fun newInstance(id: String? = null) = UserPasswordChangeFragment().apply {
            arguments = Bundle().apply {
                putString(USER_ID_KEY, id)
            }
        }

    }
}