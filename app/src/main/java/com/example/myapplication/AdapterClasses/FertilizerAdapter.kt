package com.example.myapplication.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.FertiPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.buy_ferti_item.view.*


class FertilizerAdapter(val arraylist:ArrayList<FertiPost>, val context: Context): RecyclerView.Adapter<FertilizerAdapter.ViewHolder>() {


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindItem(model: FertiPost, position: Int){
            var count=0

            itemView.name_ferti_seller.text=model.name
            itemView.address_ferti_seller.text=model.address
            itemView.phone_ferti_seller.text=model.phone
            itemView.req_ferti_seller.text=model.fertilizer_type
            itemView.buy_button.setOnClickListener {
                // itemView.address_buyer.text=model.Name
                val usersRef= FirebaseDatabase.getInstance().reference.child("availablefertilizer")
                usersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){

                            for (i in p0.children){
                                if (position==count){
                                    val ref= FirebaseDatabase.getInstance().reference.child("users")
                                            .child(i.key.toString()).child("ferti_buyers").child("uid")

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
        val view = LayoutInflater.from(context).inflate(R.layout.buy_ferti_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(arraylist[position],position)
    }

    override fun getItemCount(): Int {
        return  arraylist.size
    }
}