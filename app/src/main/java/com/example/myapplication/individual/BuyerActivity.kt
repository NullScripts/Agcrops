package com.example.myapplication.individual

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
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


    private val db = FirebaseDatabase.getInstance()
    private var id=""
    private val userMap = HashMap<String, Any>()
    private val currentTime = System.currentTimeMillis()
    private val curruserID=FirebaseAuth.getInstance().currentUser!!.uid
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var arraylist: ArrayList<User>

    var x=""
    var tim=""
    var requirements=""
    var user_name=""
    var user_address=""
    var t=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)



//       arraylist=ArrayList()
//
//        seller_recycler.layoutManager= LinearLayoutManager(this)
//
//
//            fetchsellerinfo()
       getsellerinfo()
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
                           findViewById<TextView>(R.id.originalrequirements) .setText(requirements)


                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@BuyerActivity,"failed",Toast.LENGTH_SHORT).show()

                        }

                    })
                    time.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            tim= snapshot.getValue().toString()
                            t=Utils.getTimeAgo(tim.toLong()).toString()
                            findViewById<TextView>(R.id.originaldate).setText(t)

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

        findViewById<TextView>(R.id.deleteRequest) .setOnClickListener{
            val dbref=db.reference.child("availablebuyers").child(curruserID)
            dbref.removeValue()
        }


        //setUpRecyclerView()

    }

    private fun getsellerinfo() {
        Log.e("prash","in if")
        val query=FirebaseDatabase.getInstance().reference.child("users").child(curruserID).child("sellers").child("uid")
        query.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                id=snapshot.getValue().toString()
                Log.e("id",id)
                val user_q=FirebaseDatabase.getInstance().reference.child("users").child(id).child("Name")
                user_q.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(p1: DataSnapshot) {
                        if (p1.exists()){


                            val s_name=p1.getValue().toString()
                            sname.text=s_name



                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val user_query=FirebaseDatabase.getInstance().reference.child("users").child(id).child("phone")
                user_query.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){


                            val s_phone=p0.getValue().toString()
                            sphone.text=s_phone


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

//    private fun fetchsellerinfo() {
//        Log.e("prash","in if")
//        val query=FirebaseDatabase.getInstance().reference.child("users").child(curruserID).child("sellers").child("uid")
//        query.addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                id=snapshot.getValue().toString()
//
//                val user_q=FirebaseDatabase.getInstance().reference.child("users").child(id)
//                user_q.addValueEventListener(object :ValueEventListener{
//                    override fun onDataChange(p0: DataSnapshot) {
//                        if (p0.exists()){
//
//                            for (j in p0.children){
//
//                                val s=j.getValue(User::class.java)
//                                Log.e("val",s?.Name.toString())
//
//                                arraylist.add(s!!)
//                            }
//                            recyclerView.adapter = SellerRequestAdapter(arraylist)
//                        }
//
//
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//
//
//    }

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