package com.example.myapplication.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.activities.buyer_infoActivity
import com.example.myapplication.activities.fertilizer_infoActivity
import com.example.myapplication.activities.tractor_infoActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RegisterActivity : AppCompatActivity() {
    lateinit var signupBtn: Button
    lateinit var nameEdit: EditText
    lateinit var emailEdit: EditText
    lateinit var radioGroup: RadioGroup
    lateinit var intent1: Intent
    val userMap = HashMap<String, Any>()


    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        signupBtn = findViewById(R.id.signup)
        nameEdit = findViewById(R.id.name)
        emailEdit = findViewById(R.id.email)
        radioGroup = findViewById(R.id.radioGroup)

        auth = FirebaseAuth.getInstance()
        GlobalScope.launch {
        var flag = false
        val ref = FirebaseDatabase.getInstance().reference.child("users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        if (i.key.toString().equals(FirebaseAuth.getInstance().currentUser!!.uid)) {

                            flag = true

//                            Log.e("flag",flag.toString())
//                            Toast.makeText(this@RegisterActivity,"flag",Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (flag.toString().equals("true")) {
                        //Toast.makeText(this@RegisterActivity, "yes", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    } else {

                        signupBtn.setOnClickListener {
                            showProgressbar()
                            signupUser()


                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }






    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {

            val checked = view.isChecked


            when (view.getId()) {
                R.id.radiotractor ->
                    if (checked) {
                        userMap["designation"]="tractor"
                        intent1 = Intent(this, tractor_infoActivity::class.java)
                    }
                R.id.radioferti ->
                    if (checked) {
                       userMap["designation"]="fertilizer"
                        intent1 = Intent(this, fertilizer_infoActivity::class.java)
                    }
                R.id.radioworker ->
                    if (checked) {
                        userMap["designation"]="buyer"
                        intent1 = Intent(this, buyer_infoActivity::class.java)
                    }
                R.id.radionone ->
                    if (checked) {
                        userMap["designation"]="farmer"
                        intent1 = Intent(this, MainActivity::class.java)
                    }

            }


        }

    }

    private fun signupUser() {
        if (emailEdit.text.trim().toString().isEmpty()) {
            emailEdit.error = "Please Enter Email"
            emailEdit.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailEdit.text.trim().toString()).matches()) {

            emailEdit.error = "Please Enter Valid Email"
            emailEdit.requestFocus()
            return
        }


        if (nameEdit.text.trim().toString().isEmpty()) {
            nameEdit.error = "Please Enter Full Name"
            nameEdit.requestFocus()
            return
        }

        saveUserInfo(emailEdit.text.trim().toString(), nameEdit.text.trim().toString())


    }


    private fun saveUserInfo(email: String, username: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")


        userMap["email"] = email
        userMap["Name"] = username
        userMap["phone"]=FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()


        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideProgressbar()
                    startActivity(intent1)
                }else{
                    Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun showProgressbar(){
        progressBar.show()
        progressBar.visibility= View.VISIBLE
    }

    fun hideProgressbar(){
        if(progressBar.visibility== View.VISIBLE) {
            progressBar.hide()
            progressBar.visibility = View.INVISIBLE
        }
    }

}

