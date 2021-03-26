package com.example.myapplication.ui.shop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdapterClasses.SellAdapter
import com.example.myapplication.R
import com.example.myapplication.models.SellPost
import com.google.firebase.database.*


class ShopFragment : Fragment() {

    private lateinit var shopViewModel: ShopViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<SellPost>
    //private lateinit var auth: FirebaseAuth



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       // shopViewModel = ViewModelProvider(this).get(ShopViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_shop, container, false)

//        val textView: TextView = root.findViewById(R.id.text_home)
//        shopViewModel.text.observe(viewLifecycleOwner, Observer {
////            textView.text = it
//        })

       // val textView: TextView = root.findViewById(R.id.text_home)
        //shopViewModel.text.observe(viewLifecycleOwner, Observer {
           // textView.text = it
      //  })
        arrayList=ArrayList()
        recyclerView=root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager= LinearLayoutManager(context)

        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("availablebuyers")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
               if (p0!!.exists()){
                   for(i in p0.children){
                       arrayList.clear()
                      var id=i.key.toString()
                       val ref:DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
                       ref.addValueEventListener(object : ValueEventListener {
                           override fun onDataChange(snapshot: DataSnapshot) {
                               if (snapshot.exists()){
                                   for (j in snapshot.children){
                                       if (j.key.toString().equals(id)){
                                             val seller=j.getValue(SellPost::class.java)
                                                Log.e("seller", seller.toString())
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

            }
        })




        return root
    }



}