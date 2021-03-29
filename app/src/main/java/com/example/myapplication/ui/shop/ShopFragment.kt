package com.example.myapplication.ui.shop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdapterClasses.SellAdapter
import com.example.myapplication.R
import com.example.myapplication.individual.BuyerActivity
import com.example.myapplication.models.SellPost
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.buy_item.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ShopFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<SellPost>



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_shop, container, false)


        arrayList=ArrayList()
        recyclerView=root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager= LinearLayoutManager(context)

        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("availablebuyers")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
               if (p0!!.exists()){
                   arrayList.clear()
                   for(i in p0.children){

                      var id=i.key.toString()
                       val ref:DatabaseReference = FirebaseDatabase.getInstance().reference.child("availablebuyers")
                       ref.addValueEventListener(object : ValueEventListener {
                           override fun onDataChange(snapshot: DataSnapshot) {
                               if (snapshot.exists()){
                                   for (j in snapshot.children){
                                       if (j.key.toString().equals(id)){
                                             val seller=j.getValue(SellPost::class.java)

                                                  arrayList.add(seller!!)


                                      }
                                    recyclerView.adapter = SellAdapter(arrayList,context!!)

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