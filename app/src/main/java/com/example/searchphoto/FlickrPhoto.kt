package com.example.searchphoto

import com.google.gson.annotations.SerializedName

class FlickrPhoto {

    @field:SerializedName("url_s")
    var url: String? = null

    @field:SerializedName("width_s")
    var width: String? = null

    @field:SerializedName("height_s")
    var height: String? = null
}

class FlickrPhotos {

    var photo: List<FlickrPhoto>? = null
}

class FlickrResponse {
    var stat: String? = null

    var photos: FlickrPhotos? = null
}