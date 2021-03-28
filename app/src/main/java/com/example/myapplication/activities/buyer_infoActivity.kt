package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.daos.BuyerDao
import com.example.myapplication.individual.BuyerActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_buyer_info.*

class buyer_infoActivity : AppCompatActivity() {

    private lateinit var buyerDao: BuyerDao
    val curruserID= FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_info)


        val ref = FirebaseDatabase.getInstance().reference.child("buyers")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        if (i.key.toString().equals(FirebaseAuth.getInstance().currentUser!!.uid)) {
                            val useref=FirebaseDatabase.getInstance().reference.child("buyers").child(curruserID).child("designation")
                            useref.addValueEventListener(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val data=snapshot.getValue().toString()
                                    if(data.equals("worker")){
                                        startActivity(Intent(applicationContext,BuyerActivity::class.java))
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })



//                            Log.e("flag",flag.toString())
//                            Toast.makeText(this@RegisterActivity,"flag",Toast.LENGTH_SHORT).show()
                        }
                    }


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        buyerDao = BuyerDao()

        buyer_req.setOnClickListener{
            showProgressbar()
            val address = buyer_address.text.toString()
            val requirement = buyer_buying_requirements.text.toString()
            if (address.isNotEmpty() && requirement.isNotEmpty()){
                if(buyerDao.addBuyingRequest(address,requirement)){
                    hideProgressbar()
                    startActivity(Intent(this,BuyerActivity::class.java))
                }
                else{
                    hideProgressbar()
                    Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                }
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