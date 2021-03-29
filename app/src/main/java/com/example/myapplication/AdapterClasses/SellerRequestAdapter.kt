package com.example.myapplication.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

import com.example.myapplication.models.User

import kotlinx.android.synthetic.main.seller_request.view.*

class SellerRequestAdapter(val arraylist:ArrayList<User>): RecyclerView.Adapter<SellerRequestAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(model: User) {
            itemView.seller_name.text=model.Name

            itemView.seller_phone.text=model.phone

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.seller_request,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(arraylist[position])
    }

    override fun getItemCount(): Int {
        return  arraylist.size
    }


}