package com.example.myapplication.authentication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log


class RegisterActivity : AppCompatActivity() {
    lateinit var signupBtn:Button
   lateinit var nameEdit :EditText
    lateinit var emailEdit:EditText
    lateinit var radioGroup:RadioGroup

    val map = HashMap<String, String>()
    private var retrofit: Retrofit? = null
    private var retrofitInterface: RetrofitInterface? = null
    private val BASE_URL = "http://192.168.43.2:3000"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        signupBtn = findViewById(R.id.signup)
        nameEdit = findViewById(R.id.name)
        emailEdit = findViewById(R.id.email)
        radioGroup = findViewById(R.id.radioGroup)


        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofitInterface = retrofit?.create(RetrofitInterface::class.java)

        signupBtn.setOnClickListener {
            handleSignupDialog()
        }


    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {

            val checked = view.isChecked


            when (view.getId()) {
                R.id.radiotractor ->
                    if (checked) {
                        map["tractor"]="true"
                    }
                R.id.radioferti ->
                    if (checked) {
                        map["fertilizer"]="true"
                    }
                R.id.radioworker ->
                    if (checked) {
                        map["worker"]="true"
                    }

            }
        }
    }


    private fun handleSignupDialog() {


            map["name"] = nameEdit.text.toString()
            map["email"] = emailEdit.text.toString()



            val call: Call<Void?>? = retrofitInterface!!.executeSignup(map)
            call!!.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    if (response.code() == 200) {
                        Toast.makeText(this@RegisterActivity,
                                "Signed up successfully", Toast.LENGTH_LONG).show()
                    } else if (response.code() == 400) {
                        Toast.makeText(this@RegisterActivity,
                                "Already registered", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, t.message,
                            Toast.LENGTH_LONG).show()
                }
            })
        }

}