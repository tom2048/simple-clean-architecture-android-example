package com.example.simplecleanarchitecture.users.ui.userlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplecleanarchitecture.MainRouter
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.databinding.UserListFragmentBinding
import eu.davidea.flexibleadapter.FlexibleAdapter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserListFragment : Fragment() {

    private lateinit var binding: UserListFragmentBinding

    private val viewModel: UserListViewModel by viewModel()

    private val router: MainRouter by inject()

    private val adapter: FlexibleAdapter<UserListItem> = FlexibleAdapter(listOf())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.user_list_fragment, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userList.layoutManager = LinearLayoutManager(requireActivity())
        binding.userList.adapter = adapter
        viewModel.userList.observe(viewLifecycleOwner, { adapter.updateDataSet(it) })
        viewModel.message.observe(viewLifecycleOwner, {
            AlertDialog.Builder(requireContext())
                .setMessage(it)
                .setPositiveButton(R.string.dialog_button_close) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        })
        viewModel.userActionConfirmation.observe(viewLifecycleOwner, { userId ->
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.user_delete_confirmation_message)
                .setPositiveButton(R.string.dialog_button_ok) { dialog, _ -> viewModel.deleteUserConfirmed(userId) }
                .setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        })
        viewModel.routing.observe(viewLifecycleOwner, {
            router.execute(it)
            //router.navigateTo(NavigationScreen.userEdit(null), true)
        })
        viewModel.loadUsers()
    }


    companion object {
        fun newInstance() = UserListFragment()
    }

}