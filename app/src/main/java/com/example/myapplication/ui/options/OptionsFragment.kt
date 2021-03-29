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
import com.example.myapplication.R
import com.example.myapplication.individual.BuyerActivity
import kotlinx.android.synthetic.main.fragment_options.view.*

class OptionsFragment : Fragment() {



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_options, container, false)
        root.buy.setOnClickListener {
            startActivity(Intent(context,BuyerActivity::class.java))
        }


        return root
    }
}