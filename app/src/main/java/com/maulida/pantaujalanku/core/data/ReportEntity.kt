package com.maulida.pantaujalanku.core.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReportEntity(
    var id : String? = null,
    var email : String? = null,
    var location : String? = null,
    var image : String? = null,
    var latitude : Double = 0.0,
    var longitude : Double = 0.0,
) : Parcelable