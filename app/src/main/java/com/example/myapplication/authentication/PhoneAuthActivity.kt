package com.example.myapplication.authentication


import android.os.Bundle

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PhoneAuthActivity : AppCompatActivity() {
    private var retrofit: Retrofit? = null
    private var retrofitInterface: RetrofitInterface? = null
    private val BASE_URL = "http://192.168.43.2:3000"
    lateinit var phone: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofitInterface = retrofit?.create(RetrofitInterface::class.java)

        val sendotp = findViewById<Button>(R.id.sendotp)
        val verify=findViewById<Button>(R.id.verifyBtn)
        phone = findViewById(R.id.phoneNumber)

        sendotp.setOnClickListener {
            sentotp()
        }
        verify.setOnClickListener {
            verifyotp()
        }



    }


    private fun sentotp() {
//        val map = HashMap<String, String>()
//        map["phonenumber"] = phone.text.toString()
//        map["channel"] = "sms"
        val phonenumber=phone.text.toString()
        //val channel="sms"

        val call: Call<Void?>? = retrofitInterface!!.executeLogin(phonenumber)
        call!!.enqueue(object : Callback<Void?> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                if (response.code() == 200) {
                    Toast.makeText(this@PhoneAuthActivity,
                            "OTP sent", Toast.LENGTH_LONG).show()
                } else if (response.code() == 400) {
                    Toast.makeText(this@PhoneAuthActivity,
                            "Error", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void?>, t: Throwable) {
                Toast.makeText(this@PhoneAuthActivity, t.message,
                        Toast.LENGTH_LONG).show()
            }
        })
    }


    private  fun verifyotp(){
        val otp=findViewById<EditText>(R.id.otp)
        val phonenumber=phone.text.toString()
        val map = HashMap<String, String>()
        map["phonenumber"] = phonenumber
        map["code"] = otp.text.toString()

        val call: Call<Void?>? = retrofitInterface!!.verify(map)
        call!!.enqueue(object : Callback<Void?> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                if (response.code() == 200) {
                    Toast.makeText(this@PhoneAuthActivity,
                        "OTP Verified", Toast.LENGTH_LONG).show()
                } else if (response.code() == 400) {
                    Toast.makeText(this@PhoneAuthActivity,
                        "Error", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void?>, t: Throwable) {
                Toast.makeText(this@PhoneAuthActivity, t.message,
                    Toast.LENGTH_LONG).show()
            }
        })

    }

    }


