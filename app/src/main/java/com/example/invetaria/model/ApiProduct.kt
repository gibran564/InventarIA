package com.example.invetaria.model

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiProduct(
    @JsonProperty("title")
    val title: String? = null,

    @JsonProperty("category")
    val category: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(category)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ApiProduct> {
        override fun createFromParcel(parcel: Parcel): ApiProduct = ApiProduct(parcel)
        override fun newArray(size: Int): Array<ApiProduct?> = arrayOfNulls(size)
    }
}