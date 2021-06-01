package com.maulida.pantaujalanku.core.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserEntity(
    var username : String? = null,
    var email : String? = null,
    var password : String? = null,
    var photo : String? = null
) : Parcelable