package com.example.myapplication.individual

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_buyer.*
import kotlinx.android.synthetic.main.buying_history_item.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuyerActivity : AppCompatActivity() {

//    private lateinit var adapter: BuyingHistoryAdapter
//    lateinit var buyerDao: BuyerDao
    val db = FirebaseDatabase.getInstance()
    val userMap = HashMap<String, Any>()
    val currentTime = System.currentTimeMillis()
    val curruserID=FirebaseAuth.getInstance().currentUser!!.uid

    var x=""
    var tim=""
    var requirements=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)

        fab_buyer.setOnClickListener{
            //buyerDao.

            val view = layoutInflater.inflate(R.layout.buying_history_item,null)
            history.addView(view)

            addAvailableBuyers(curruserID)
            GlobalScope.launch {
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
                        val t=Utils.getTimeAgo(tim.toLong())
                        originaldate.setText(t)
                    }

                    override fun onCancelled(error: DatabaseError) {


                    }

                })
            }

        }


        //setUpRecyclerView()

    }

    fun addAvailableBuyers(curruserID: String ){



        val reference = db.reference.child("availablebuyers")
        val query=db.reference.child("buyers").child(curruserID).child("requirements")
        //val ref=db.reference.child("buyers")
        query.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               x=  snapshot.getValue().toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })







        userMap["currentTime"] = currentTime
        userMap["phone"]=FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()
        userMap["requirements"]=x

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