package com.example.myapplication.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.SellPost
import kotlinx.android.synthetic.main.buy_item.view.*

class SellAdapter(val arraylist:ArrayList<SellPost>,val context: Context): RecyclerView.Adapter<SellAdapter.ViewHolder>() {

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindItem(model:SellPost){
            itemView.name_buyer.text=model.Name
            itemView.address_buyer.text=model.email

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.buy_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(arraylist[position])
    }

    override fun getItemCount(): Int {
        return  arraylist.size
    }
}