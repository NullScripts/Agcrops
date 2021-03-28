package com.example.myapplication.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Common
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.activities.buyer_infoActivity
import com.example.myapplication.activities.fertilizer_infoActivity
import com.example.myapplication.activities.tractor_infoActivity
import com.example.myapplication.models.UserInfoModel
import com.example.myapplication.utils.UserUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RegisterActivity : AppCompatActivity() {
    lateinit var signupBtn: Button
    lateinit var nameEdit: EditText
    lateinit var emailEdit: EditText
    lateinit var radioGroup: RadioGroup
    lateinit var intent1: Intent

    private lateinit var profilePic : ImageView

    private lateinit var storageReference: StorageReference
    private var imageUri : Uri? = null

    private lateinit var waitingDialog: AlertDialog


    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        storageReference = FirebaseStorage.getInstance().getReference()

        signupBtn = findViewById(R.id.signup)
        nameEdit = findViewById(R.id.name)
        radioGroup = findViewById(R.id.radioGroup)
        profilePic = findViewById(R.id.profile_image)

        waitingDialog= AlertDialog.Builder(this)
                .setMessage("Waiting")
                .setCancelable(false)
                .create()

        if(Common.currentUser != null && Common.currentUser!!.avatar != null && !TextUtils.isEmpty((Common.currentUser!!.avatar)))
        {
            Glide.with(this)
                    .load(Common.currentUser!!.avatar)
                    .into(profilePic)
        }

        profilePic.setOnClickListener {
            val intent = Intent()
            intent.setType("image/")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        auth = FirebaseAuth.getInstance()
        GlobalScope.launch {
        var flag = false
        val ref = FirebaseDatabase.getInstance().reference.child("users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showProgressbar()
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        if (i.key.toString().equals(FirebaseAuth.getInstance().currentUser!!.uid)) {

                            flag = true


                        }
                    }
                    if (flag.toString().equals("true")) {
                        //Toast.makeText(this@RegisterActivity, "yes", Toast.LENGTH_SHORT).show()
                        hideProgressbar()
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

            }

        })
    }






    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {

            val checked = view.isChecked

            val userModel = UserInfoModel()


            when (view.getId()) {
                R.id.radiotractor ->
                    if (checked) {
                        userModel.designation="tractor"
                        Common.currentUser = userModel
                        intent1 = Intent(this, tractor_infoActivity::class.java)
                    }
                R.id.radioferti ->
                    if (checked) {
                        userModel.designation="fertilizer"
                        Common.currentUser = userModel
                        intent1 = Intent(this, fertilizer_infoActivity::class.java)
                    }
                R.id.radioworker ->
                    if (checked) {
                        userModel.designation="buyer"
                        Common.currentUser = userModel
                        intent1 = Intent(this, buyer_infoActivity::class.java)
                    }
                R.id.radionone ->
                    if (checked) {
                        userModel.designation="farmer"
                        Common.currentUser = userModel
                        intent1 = Intent(this, MainActivity::class.java)
                    }

            }


        }

    }

    private fun signupUser() {

        val userModel = UserInfoModel()

        if (nameEdit.text.trim().toString().isEmpty()) {
            nameEdit.error = "Please Enter Full Name"
            nameEdit.requestFocus()
            return
        }

        userModel.name = nameEdit.text.trim().toString()

        if(FirebaseAuth.getInstance().currentUser!!.phoneNumber != null &&
                !TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber))
            userModel.phoneNumber = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()

        saveUserInfo(userModel)
    }


    private fun saveUserInfo(userModel : UserInfoModel) {

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

        usersRef.child(currentUserId).setValue(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideProgressbar()
                    Common.currentUser = userModel
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

    companion object{
        val PICK_IMAGE_REQUEST = 7272
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.data != null)
            {
                imageUri = data.data
                profilePic.setImageURI(imageUri)

                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle("Select Avatar")
                .setMessage("Do you really want to select this avatar?")
                .setNegativeButton("CANCEL", {dialogInterface, _ -> dialogInterface.dismiss()})
                .setPositiveButton("SELECT") {dialogInterface, _ ->

                    if(imageUri != null)
                    {
                        waitingDialog.show()
                        val avatarFolder = storageReference.child("avatars/"+FirebaseAuth.getInstance().currentUser!!.uid)
                        avatarFolder.putFile(imageUri!!)
                                .addOnFailureListener{e ->
                                    Snackbar.make(register_layout, e.message!!, Snackbar.LENGTH_LONG).show()
                                    waitingDialog.dismiss()
                                }
                                .addOnCompleteListener{task ->
                                    if(task.isSuccessful)
                                    {
                                        avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                            val update_data = HashMap<String, Any>()
                                            update_data.put("avatar", uri.toString())

                                            UserUtils.updateUser(register_layout, update_data)
                                        }
                                    }
                                    waitingDialog.dismiss()
                                }
                                .addOnProgressListener { taskSnapshot ->
                                    val progress = (100.0*taskSnapshot.bytesTransferred/ taskSnapshot.totalByteCount)
                                    waitingDialog.setMessage(StringBuilder("Uploading: ").append(progress).append("%"))
                                }
                    }

                }.setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener{
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.colorAccent))
        }
        dialog.show()
    }

}

