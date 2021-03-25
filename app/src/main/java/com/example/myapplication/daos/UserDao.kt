package com.example.myapplication.daos

import android.content.ContentValues.TAG
import android.util.Log
import com.example.myapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    val db = FirebaseDatabase.getInstance()
    val reference = db.getReference("users")

    fun addUser(user: User?){
        val currentUser = FirebaseAuth.getInstance().getCurrentUser()!!.getUid();
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                reference.child(currentUser).setValue(currentUser)
            }
        }
    }

    fun getUserById(uId: String){
        val rootRef = db.reference
        val ordersRef = rootRef.child("users").orderByChild("uid").equalTo(uId)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val name = ds.child("name").getValue(String::class.java)
//                    Log.d(TAG, name)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.getMessage()) //Don't ignore errors!
            }
        }
        ordersRef.addListenerForSingleValueEvent(valueEventListener)
    }

}