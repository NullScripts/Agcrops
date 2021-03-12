package com.example.myapplication.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.edit.EditTextViewModel
import com.example.myapplication.R


class EditTextFragment : Fragment() {

    private lateinit var editTextViewModel: EditTextViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        editTextViewModel =
                ViewModelProvider(this).get(EditTextViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_edit_text, container, false)
        val textView: TextView = root.findViewById(R.id.text_edit)
        editTextViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}