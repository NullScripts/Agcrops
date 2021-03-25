package com.example.myapplication.AdapterClasses

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Utils
import com.example.myapplication.models.BuyerRequest
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions

class BuyingHistoryAdapter(options: FirebaseRecyclerOptions<BuyerRequest>) : FirebaseRecyclerAdapter<BuyerRequest, BuyingHistoryAdapter.BuyingHistoryViewHolder>(
        options) {

    class BuyingHistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val buyingHistoryDate: TextView = itemView.findViewById(R.id.originaldate)
        val buyingHistoryRequirements: TextView = itemView.findViewById(R.id.originalrequirements)
        val buyingHistoryDeleteButton: Button = itemView.findViewById(R.id.deleteRequest)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyingHistoryViewHolder {
        return BuyingHistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.buying_history_item, parent, false))
    }

    override fun onBindViewHolder(holder: BuyingHistoryViewHolder, position: Int, model: BuyerRequest) {
        holder.buyingHistoryDate.text = Utils.getTimeAgo(model.createddate)
        holder.buyingHistoryRequirements.text = model.requirements

    }

}