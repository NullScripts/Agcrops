package com.example.myapplication.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.Common
import com.example.myapplication.R
import com.example.myapplication.individual.TractorRentingActivity
import com.example.myapplication.models.TractorInfoModel
import com.example.myapplication.utils.UserUtils
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

class tractor_infoActivity : AppCompatActivity() {

    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var listener : FirebaseAuth.AuthStateListener

    private lateinit var database : FirebaseDatabase
    private lateinit var tractorInfoRef : DatabaseReference


    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if(firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tractor_info)

        init()
    }

    private fun init()
    {
        database = FirebaseDatabase.getInstance()
        tractorInfoRef = database.getReference(Common.TRACTOR_INFO_REFERENCE)

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if(user != null) {
                FirebaseInstanceId.getInstance()
                    .instanceId
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@tractor_infoActivity,
                            e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnSuccessListener { instanceIdResult ->
                        Log.d("TOKEN", instanceIdResult.token)
                        UserUtils.updateToken(this@tractor_infoActivity, instanceIdResult.token)

                    }
                checkUserFromFirebase()
            }
            else
                registerTractor()
        }

    }

    private fun checkUserFromFirebase() {
        tractorInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@tractor_infoActivity,error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        Toast.makeText(this@tractor_infoActivity,"User already registered!", Toast.LENGTH_SHORT).show()
                        val model = snapshot.getValue(TractorInfoModel::class.java)
                        goToTratorRentingActivity(model)
                    }
                    else
                    {
                        registerTractor()
                    }
                }
            })
    }

    private fun goToTratorRentingActivity(model: TractorInfoModel?) {
        Common.currentTractor = model
        startActivity(Intent(this, TractorRentingActivity::class.java))
        finish()
    }

    private fun registerTractor() {


        val trac_phn = findViewById<View>(R.id.trac_phn) as EditText
        val trac_num = findViewById<View>(R.id.trac_num) as EditText
        val trac_type = findViewById<View>(R.id.trac_type) as EditText
        val trac_own_name = findViewById<View>(R.id.trac_own_name) as EditText
        val trac_own_last_name = findViewById<View>(R.id.trac_own_last_name) as EditText
        val trac_info_sub = findViewById<View>(R.id.trac_info_sub) as Button

        //set Data
        if(FirebaseAuth.getInstance().currentUser!!.phoneNumber != null &&
            !TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber))
            trac_phn.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)

        //Event
        trac_info_sub.setOnClickListener {
            if(TextUtils.isDigitsOnly(trac_num.text.toString()))
            {
                Toast.makeText(this@tractor_infoActivity, "Please Enter Tractor's Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(TextUtils.isDigitsOnly(trac_type.text.toString()))
            {
                Toast.makeText(this@tractor_infoActivity, "Please Enter Tractor's Type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(TextUtils.isDigitsOnly(trac_own_name.text.toString()))
            {
                Toast.makeText(this@tractor_infoActivity, "Please Enter Tractor Owners First Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(TextUtils.isDigitsOnly(trac_own_last_name.text.toString()))
            {
                Toast.makeText(this@tractor_infoActivity, "Please Enter Tractor Owner's Last Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else
            {
                val model = TractorInfoModel()
                model.tractorNumber = trac_num.text.toString()
                model.type = trac_type.text.toString()
                model.phoneNumber = trac_phn.text.toString()
                model.ownerFirstName = trac_own_name.text.toString()
                model.ownerLastName = trac_own_last_name.text.toString()

                tractorInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(model)
                    .addOnFailureListener{e ->
                        Toast.makeText(this@tractor_infoActivity, ""+e.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnSuccessListener {
                        Toast.makeText(this@tractor_infoActivity, "Register Successfully!", Toast.LENGTH_SHORT).show()

                        goToTratorRentingActivity(model)
                    }
            }
        }
    }

}