package com.example.myapplication.individual

import android.app.DownloadManager
import android.content.AbstractThreadedSyncAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.AdapterClasses.BuyingHistoryAdapter
import com.example.myapplication.R
import com.example.myapplication.activities.buyer_infoActivity
import com.example.myapplication.daos.BuyerDao
import com.example.myapplication.models.BuyerRequest
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_buyer.*

class BuyerActivity : AppCompatActivity() {

    private lateinit var adapter: BuyingHistoryAdapter
    lateinit var buyerDao: BuyerDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)

        fab_buyer.setOnClickListener{
            buyerDao.addAvailableBuyers(FirebaseAuth.getInstance().currentUser!!.uid)
        }

        setUpRecyclerView()

    }

    private fun setUpRecyclerView() {
        val historyReference = buyerDao.reference
        val query = historyReference.equalTo(FirebaseAuth.getInstance().currentUser!!.uid).orderByChild("currentTime")
        val recyclerViewHistoryOptions = FirebaseRecyclerOptions.Builder<BuyerRequest>().setQuery(query, BuyerRequest::class.java).build()

        adapter = BuyingHistoryAdapter(recyclerViewHistoryOptions)
        recycler_view_history.adapter = adapter
        recycler_view_history.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}