package com.example.searchphoto

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrPhotoService {

    @GET("services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1&extras=url_s")
    fun getPhotos(
        @Query("text") query: String,
        @Query("page") page: Int,
        @Query("per_page") size: Int,
        @Query("api_key") key: String
    ): Call<FlickrResponse>
}