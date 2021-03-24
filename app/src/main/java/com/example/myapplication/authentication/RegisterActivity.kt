package com.example.myapplication.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.activities.buyer_infoActivity
import com.example.myapplication.activities.fertilizer_infoActivity
import com.example.myapplication.activities.tractor_infoActivity
import kotlinx.android.synthetic.main.activity_register.*

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
    lateinit var intent1:Intent
    val map = HashMap<String, String>()
    private var retrofit: Retrofit? = null
    private var retrofitInterface: RetrofitInterface? = null
    private val BASE_URL = "http://192.168.1.100:3000"
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
            //Dummy Intent for Practice
            //TODO: Remove the intent from here after fixing handleSignupDialog intent
          // val intent = Intent(this,MainActivity::class.java)
           startActivity(intent1)
            handleSignupDialog(this)




        }


    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {

            val checked = view.isChecked
            intent1 = Intent(this,MainActivity::class.java)

            when (view.getId()) {
                R.id.radiotractor ->
                    if (checked) {
                        map["tractor"]="true"
                        intent1 = Intent(this,tractor_infoActivity::class.java)
                    }
                R.id.radioferti ->
                    if (checked) {
                        map["fertilizer"]="true"
                        intent1 = Intent(this,fertilizer_infoActivity::class.java)
                    }
                R.id.radioworker ->
                    if (checked) {
                        map["worker"]="true"
                         intent1 = Intent(this,buyer_infoActivity::class.java)
                    }

            }

        }

    }


    private fun handleSignupDialog(context: Context) {


            map["name"] = nameEdit.text.toString()
            map["email"] = emailEdit.text.toString()



            val call: Call<Void?>? = retrofitInterface!!.executeSignup(map)
            call!!.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    if (response.code() == 200) {
                        Toast.makeText(this@RegisterActivity,
                                "Signed up successfully", Toast.LENGTH_LONG).show()

                        val intent = Intent(context,MainActivity::class.java)
                        startActivity(intent)
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