package com.example.cycu_notification_system
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/home/mvc/ann.Model.jsp")
    fun getData(@Query("sn_type") snType: String): Call<List<JsonModel.DataModel>>
}
