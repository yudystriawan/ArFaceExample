package com.example.arfaceexample.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Filter(
    var photo: String?,
    var name: String?,
    var model: Int?
) : Parcelable