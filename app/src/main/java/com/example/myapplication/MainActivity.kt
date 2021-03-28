package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bumptech.glide.Glide
import com.example.myapplication.authentication.RegisterActivity
import com.example.myapplication.models.UserInfoModel
import com.example.myapplication.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference

    private lateinit var navView : NavigationView
    private lateinit var drawerLayout : DrawerLayout

    private lateinit var img_avatar : ImageView
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference

    private var imageUri : Uri? = null

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)



        //Bottom Navigation
        navController = findNavController(R.id.nav_host_fragment)
        bottom_navigation.setupWithNavController(navController)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView= findViewById(R.id.nav_view)

        //Navigation Up button
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController)

        setupActionBarWithNavController(navController, appBarConfiguration)

        NavigationUI.setupWithNavController(navView, navController)

        initViews()
        init()
    }

    private fun initViews() {
        val headerView = navView.getHeaderView(0)
        val txt_name = headerView.findViewById<View>(R.id.txt_name) as TextView
        val txt_phone = headerView.findViewById<View>(R.id.txt_phone) as TextView
        val txt_designation =  headerView.findViewById<View>(R.id.txt_designation) as TextView
        img_avatar = headerView.findViewById<View>(R.id.image_avatar) as ImageView

        txt_name.setText(Common.buildWelcomeMessage())
        txt_phone.setText(Common.currentUser!!.phoneNumber)
        txt_designation.setText(StringBuilder().append(Common.currentUser!!.designation))

        if(Common.currentUser != null && Common.currentUser!!.avatar != null && !TextUtils.isEmpty((Common.currentUser!!.avatar)))
        {
            Glide.with(this)
                    .load(Common.currentUser!!.avatar)
                    .into(img_avatar)
        }

        img_avatar.setOnClickListener{
            val intent = Intent()
            intent.setType("image/")
            intent.setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun init() {
        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null) {
                FirebaseInstanceId.getInstance()
                    .instanceId
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@MainActivity,
                            e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnSuccessListener { instanceIdResult ->
                        Log.d("TOKEN", instanceIdResult.token)
                        UserUtils.updateToken(this@MainActivity, instanceIdResult.token)

                    }
                checkUserFromFirebase()
            }

        }
    }

    private fun checkUserFromFirebase() {
        userInfoRef
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity,error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists())
                        {
                            // Toast.makeText(this@SplashScreenActivity,"User already registered!", Toast.LENGTH_SHORT).show()
                            val model = snapshot.getValue(UserInfoModel::class.java)
                            Common.currentUser = model
                        }
                        else
                        {
                            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                            finish()
                        }
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.data != null)
            {
                imageUri = data.data
                img_avatar.setImageURI(imageUri)

                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Change Avatar")
                .setMessage("Do you really want to change avatar?")
                .setNegativeButton("CANCEL", {dialogInterface, _ -> dialogInterface.dismiss()})
                .setPositiveButton("CHANGE") {dialogInterface, _ ->

                    if(imageUri != null)
                    {
                        waitingDialog.show()
                        val avatarFolder = storageReference.child("avatars/"+FirebaseAuth.getInstance().currentUser!!.uid)
                        avatarFolder.putFile(imageUri!!)
                                .addOnFailureListener{e ->
                                    Snackbar.make(drawerLayout, e.message!!, Snackbar.LENGTH_LONG).show()
                                    waitingDialog.dismiss()
                                }
                                .addOnCompleteListener{task ->
                                    if(task.isSuccessful)
                                    {
                                        avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                            val update_data = HashMap<String, Any>()
                                            update_data.put("avatar", uri.toString())

                                            UserUtils.updateUser(drawerLayout, update_data)
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
                    .setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
        }
        dialog.show()
    }

    companion object{
        val PICK_IMAGE_REQUEST = 7272
    }
}