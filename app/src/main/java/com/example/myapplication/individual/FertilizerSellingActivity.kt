package com.example.myapplication.individual

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_fertilizer_info.*
import kotlinx.android.synthetic.main.activity_fertilizer_selling.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FertilizerSellingActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance()
    private var id=""
    private val userMap = HashMap<String, Any>()
    private val currentTime = System.currentTimeMillis()
    private val curruserID=FirebaseAuth.getInstance().currentUser!!.uid

    var x1=""

    var fertilizer_type=""
    var user_name1=""
    var user_address1=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fertilizer_selling)



        val ref=db.reference.child("availablefertilizer")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val req=db.reference.child("availablefertilizer").child(curruserID).child("fertilizer type")


                    req.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            fertilizer_type=  snapshot.getValue().toString()
                            findViewById<TextView>(R.id.val_ferti_type) .setText(fertilizer_type)


                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@FertilizerSellingActivity,"failed", Toast.LENGTH_SHORT).show()

                        }

                    })



                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        fab_ferti.setOnClickListener{


            addAvailableFertilizers(curruserID)

        }

        findViewById<TextView>(R.id.deletefertiRequest) .setOnClickListener{
            val dbref=db.reference.child("availablefertilizer").child(curruserID)
            dbref.removeValue()
        }



        //setUpRecyclerView()

    }




    fun addAvailableFertilizers(curruserID: String ){



        val reference = db.reference.child("availablefertilizer")
        val query=db.reference.child("fertilizer").child(curruserID).child("fertilizer_type")
        val user_ref = db.reference.child("users").child(curruserID).child("Name")
        val user_addr=db.reference.child("fertilizer").child(curruserID).child("address")

        //val ref=db.reference.child("buyers")
        GlobalScope.launch {
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    x1=  snapshot.getValue().toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }
        GlobalScope.launch {
            user_ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user_name1=  snapshot.getValue().toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }


        user_addr.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user_address1=  snapshot.getValue().toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })





        userMap["phone"]= FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()
        userMap["fertilizer_type"]=x1
        userMap["name"]=user_name1
        userMap["address"]=user_address1

        reference.child(curruserID).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {


                    }
                }
        //   }

    }




}