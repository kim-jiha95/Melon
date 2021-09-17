package com.melon.service

import retrofit2.Call
import retrofit2.http.GET

interface MusicService {

    @GET("/v3/3b14733c-b1fc-40ee-bf62-ac937aaa6708")
    fun listMusics() : Call<MusicDto>

}