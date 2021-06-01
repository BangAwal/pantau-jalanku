package com.maulida.pantaujalanku.core.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReportEntity(
    var location : String? = null,
    var image : String? = null
) : Parcelable