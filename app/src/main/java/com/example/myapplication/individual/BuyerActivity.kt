package com.example.myapplication.individual

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdapterClasses.SellerRequestAdapter
import com.example.myapplication.R
import com.example.myapplication.Utils
import com.example.myapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_buyer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuyerActivity : AppCompatActivity() {


    val db = FirebaseDatabase.getInstance()
    var id=""
    val userMap = HashMap<String, Any>()
    val currentTime = System.currentTimeMillis()
    val curruserID=FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var recyclerView: RecyclerView
    private lateinit var arraylist: ArrayList<User>

    var x=""
    var tim=""
    var requirements=""
    var user_name=""
    var user_address=""
    var t=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)



       arraylist=ArrayList()

        seller_recycler.layoutManager= LinearLayoutManager(this)


            fetchsellerinfo()
//

        val ref=db.reference.child("availablebuyers")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val req=db.reference.child("availablebuyers").child(curruserID).child("requirements")

                    val time=db.reference.child("availablebuyers").child(curruserID).child("currentTime")
                    req.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            requirements=  snapshot.getValue().toString()
                            originalrequirements.setText(requirements)


                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                    time.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            tim= snapshot.getValue().toString()
                            t=Utils.getTimeAgo(tim.toLong()).toString()
                            originaldate.setText(t)

                        }

                        override fun onCancelled(error: DatabaseError) {


                        }

                    })


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        fab_buyer.setOnClickListener{

            addAvailableBuyers(curruserID)

        }

        deleteRequest.setOnClickListener{
            val dbref=db.reference.child("availablebuyers").child(curruserID)
            dbref.removeValue()
        }


        //setUpRecyclerView()

    }

    private fun fetchsellerinfo() {
        Log.e("prash","in if")
        val query=FirebaseDatabase.getInstance().reference.child("users").child(curruserID).child("sellers").child("uid")
        query.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                id=snapshot.getValue().toString()
                Log.e("id",id)
                val user_q=FirebaseDatabase.getInstance().reference.child("users").child(id)
                user_q.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){

                            for (j in snapshot.children){
                                val seller=j.getValue(User::class.java)
                                arraylist.add(seller!!)
                            }
                            recyclerView.adapter = SellerRequestAdapter(arraylist,this@BuyerActivity)
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    fun addAvailableBuyers(curruserID: String ){



        val reference = db.reference.child("availablebuyers")
        val query=db.reference.child("buyers").child(curruserID).child("requirements")
        val user_ref = db.reference.child("users").child(curruserID).child("Name")
        val user_addr=db.reference.child("buyers").child(curruserID).child("address")

        //val ref=db.reference.child("buyers")
        GlobalScope.launch {
            query.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    x=  snapshot.getValue().toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }
        GlobalScope.launch {
            user_ref.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user_name=  snapshot.getValue().toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }


        user_addr.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user_address=  snapshot.getValue().toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })




        userMap["currentTime"] = currentTime
        userMap["phone"]=FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()
        userMap["requirements"]=x
        userMap["name"]=user_name
        userMap["address"]=user_address

        reference.child(curruserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {


                }
            }
        //   }

    }

//    private fun setUpRecyclerView() {
//        val historyReference = FirebaseDatabase.getInstance().reference.child("availablebuyers")
//        val query = historyReference//.equalTo(FirebaseAuth.getInstance().currentUser!!.uid).orderByChild("currentTime")
//        val recyclerViewHistoryOptions = FirebaseRecyclerOptions.Builder<BuyerRequest>().setQuery(query, BuyerRequest::class.java).build()
//
//        adapter = BuyingHistoryAdapter(recyclerViewHistoryOptions)
//        recycler_view_history.adapter = adapter
//        recycler_view_history.layoutManager = LinearLayoutManager(this)
//    }
//
//    override fun onStart() {
//        super.onStart()
//        adapter.startListening()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        adapter.stopListening()
//    }



}