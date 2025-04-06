package com.example.cs320_hospital_and_medical_android_app

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cs320_hospital_and_medical_android_app.databinding.ItemUserTemplateBinding

class RecyclerViewAdapter(private val userList: MutableList<UserModelData>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    // ViewHolder class using ViewBinding
    inner class MyViewHolder(val binding: ItemUserTemplateBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflate the item layout and create the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemUserTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    // Bind data to the views
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = userList[position]

        // Set the text for each view
        holder.binding.userIdTextView.text = user.userID
        holder.binding.accountIDTextView.text = user.accountID
        holder.binding.roleTextView.text = user.userRole
        holder.binding.fullNameTextView.text = user.fullName

        // Delete button logic
        holder.binding.deleteUserButton.setOnClickListener {
            val userID = holder.binding.userIdTextView.text.toString()
            val userRole = holder.binding.roleTextView.text.toString()

            val dbHandler = DBHandlerClass()
            dbHandler.deleteAccountByUserId(userID, userRole) { success ->
                if (success) {
                    val positionToRemove = holder.adapterPosition
                    if (positionToRemove != RecyclerView.NO_POSITION) {
                        removeItem(positionToRemove)
                    }
                } else {
                    Log.e("DEBUG", "ERROR: Account deletion failed")
                }
            }
        }
    }

    // Return total item count
    override fun getItemCount(): Int = userList.size

    // Remove item from the list and notify adapter
    fun removeItem(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
    }
}
