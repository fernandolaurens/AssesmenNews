package com.laurens.assesmennews.data.local.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsEntity(
    val id: Int,
    val title: String,
    val summary: String,
    val publishedAt: String,
    val url: String,
    val urlToImage: String
) : Parcelable