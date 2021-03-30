package com.example.myapplication.AdapterClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.individual.BuyerActivity
import com.example.myapplication.models.SellPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.buy_item.view.*
import kotlinx.android.synthetic.main.fragment_book.view.*

class SellAdapter(val arraylist:ArrayList<SellPost>,val context: Context): RecyclerView.Adapter<SellAdapter.ViewHolder>() {


    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindItem(model:SellPost,position: Int){
            var count=0
            //var name=""
            //val map=HashMap<String,Any>()
            itemView.name_buyer.text=model.Name
            itemView.address_buyer.text=model.address
            itemView.phone_buyer.text=model.phone
            itemView.req_buyer.text=model.requirements
            itemView.sell_button.setOnClickListener {
               // itemView.address_buyer.text=model.Name
                val usersRef= FirebaseDatabase.getInstance().reference.child("availablebuyers")
                usersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){

                            for (i in p0.children){
                                if (position==count){
                                    val ref=FirebaseDatabase.getInstance().reference.child("users")
                                            .child(i.key.toString()).child("sellers").child("uid")

                                    ref.setValue(FirebaseAuth.getInstance().currentUser!!.uid)



                                }
                                count=count+1


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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.buy_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(arraylist[position],position)
    }

    override fun getItemCount(): Int {
        return  arraylist.size
    }
}