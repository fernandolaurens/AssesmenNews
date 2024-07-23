package com.laurens.assesmennews.data.remote.response

import com.google.gson.annotations.SerializedName

data class NewsResponse(

    @field:SerializedName("next")
    val next: String? = null,

    @field:SerializedName("previous")
    val previous: Any? = null,

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("results")
    val results: List<ResultsItem?>? = null
)

data class ResultsItem(

    @field:SerializedName("summary")
    val summary: String? = null,

    @field:SerializedName("news_site")
    val newsSite: String? = null,

    @field:SerializedName("featured")
    val featured: Boolean? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("published_at")
    val publishedAt: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("launches")
    val launches: List<LaunchesItem?>? = null,

    @field:SerializedName("events")
    val events: List<Any?>? = null
)

data class LaunchesItem(

    @field:SerializedName("launch_id")
    val launchId: String? = null,

    @field:SerializedName("provider")
    val provider: String? = null
)