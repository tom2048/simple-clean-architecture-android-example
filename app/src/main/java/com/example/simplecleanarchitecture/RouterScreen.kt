package com.example.simplecleanarchitecture

import com.example.simplecleanarchitecture.users.ui.passwordchange.UserPasswordChangeFragment
import com.example.simplecleanarchitecture.users.ui.useredit.UserEditFragment
import com.example.simplecleanarchitecture.users.ui.userlist.UserListFragment
import com.github.terrakok.cicerone.androidx.ActivityScreen
import com.github.terrakok.cicerone.androidx.FragmentScreen

object RouterScreen {
    class UserListScreen() : FragmentScreen(fragmentCreator = { UserListFragment.newInstance() })
    data class UserEditScreen(val id: String?) : FragmentScreen(fragmentCreator = { UserEditFragment.newInstance(id) })
    data class UserPasswordChangeScreen(val id: String?) : FragmentScreen(fragmentCreator = { UserPasswordChangeFragment.newInstance(id) })
}

