package com.example.myapplication.authentication

import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.HashMap

interface RetrofitInterface {


    @POST("/signup")
    fun executeSignup(@Body map: HashMap<String, String>): Call<Void?>?
    @GET("/login")
    fun executeLogin(@Query("phonenumber")  phonenumber:String): Call<Void?>?
    @GET("/verify")
    fun verify(@QueryMap map:HashMap<String, String>): Call<Void?>?
}