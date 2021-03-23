package com.example.simplecleanarchitecture.users.ui.userlist

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

class UserListItem(
    val user: UserDetails,
    val editClickItem: ((String) -> Unit)? = null,
    val deleteClickItem: ((String) -> Unit)? = null,
    val changePasswordButton: ((String) -> Unit)? = null
): AbstractFlexibleItem<UserListItem.UserViewHolder>() {

    override fun equals(other: Any?): Boolean = user == other

    override fun getLayoutRes(): Int = R.layout.user_list_item

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): UserViewHolder {
        return UserViewHolder(view, adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: UserViewHolder,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        holder.nickname.text = user.nickname
        holder.email.text = user.email
        holder.editButton.setOnClickListener { editClickItem?.invoke(user.id!!) }
        holder.deleteButton.setOnClickListener { deleteClickItem?.invoke(user.id!!) }
        holder.changePasswordButton.setOnClickListener { changePasswordButton?.invoke(user.id!!) }
    }

    class UserViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>) : FlexibleViewHolder(view, adapter) {
        val nickname: TextView = view.findViewById(R.id.nickname)
        val email: TextView = view.findViewById(R.id.email)
        val changePasswordButton: View = view.findViewById(R.id.changePasswordButton)
        val editButton: View = view.findViewById(R.id.editButton)
        val deleteButton: View = view.findViewById(R.id.deleteButton)

    }

}