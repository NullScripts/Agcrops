package com.example.myapplication.daos

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FertiDao {
    val db = FirebaseDatabase.getInstance()
    val reference = db.reference.child("fertilizer")
    var flag:Boolean=false

    fun addSellingRequest(address: String , ferti_type: String):Boolean{
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        GlobalScope.launch {

            val userMap = HashMap<String, Any>()
            userMap["address"] = address
            userMap["fertilizer type"] = ferti_type



            reference.child(currentUser).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        flag=true

                    }
                }
        }
        return flag
    }

}