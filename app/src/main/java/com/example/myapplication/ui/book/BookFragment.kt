package com.example.myapplication.ui.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R


class BookFragment : Fragment() {

    private lateinit var bookViewModel: BookViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        bookViewModel =
                ViewModelProvider(this).get(BookViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_book, container, false)
        val textView: TextView = root.findViewById(R.id.text_edit)
        bookViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}