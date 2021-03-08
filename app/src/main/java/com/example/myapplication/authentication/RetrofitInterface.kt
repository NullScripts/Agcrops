package com.example.myapplication.authentication

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import kotlin.collections.HashMap

interface RetrofitInterface {


    @POST("/signup")
    fun executeSignup(@Body map: HashMap<String, String>): Call<Void?>?
    @GET("/login")
    fun executeLogin(@Query("phonenumber")  phonenumber:String): Call<Void?>?
    @POST("/verify")
    fun verify(@Body map: HashMap<String, String>): Call<Void?>?
}