package com.maulida.pantaujalanku.core.preference

import android.content.Context

class SetPreferences(context : Context) {

    companion object{
        const val KEY_IS_LOGIN = "key_login"
    }

    private val pref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val editor = pref.edit()

    fun createLoginSession(){
        editor.putBoolean(KEY_IS_LOGIN, true).commit()
    }

    fun logOut(){
        editor.clear()
        editor.commit()
    }

    val isLogin = pref.getBoolean(KEY_IS_LOGIN, false)

    fun saveToPreference(key : String, value : String) = editor.putString(key,value).commit()

    fun getFromPreference(key : String) = pref.getString(key, "")




}