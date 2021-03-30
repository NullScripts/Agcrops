package com.example.myapplication.ui.options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdapterClasses.FertilizerAdapter
import com.example.myapplication.AdapterClasses.SellAdapter
import com.example.myapplication.R
import com.example.myapplication.individual.BuyerActivity
import com.example.myapplication.models.FertiPost
import com.example.myapplication.models.SellPost
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_options.view.*

class OptionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<FertiPost>



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_options, container, false)

        arrayList=ArrayList()
        recyclerView=root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager= LinearLayoutManager(context)

        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("availablefertilizer")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0!!.exists()){
                    arrayList.clear()
                    for(i in p0.children){

                        var id=i.key.toString()
                        val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("availablefertilizer")
                        ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    for (j in snapshot.children){
                                        if (j.key.toString().equals(id)){
                                            val seller=j.getValue(FertiPost::class.java)

                                            arrayList.add(seller!!)


                                        }
                                        recyclerView.adapter = FertilizerAdapter(arrayList,context!!)

                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })



                    }


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //val sell_req=FirebaseDatabase.getInstance().reference.child("availablebuyers").child(curruserID).child("requirements")

            }
        })



        return root
    }
}