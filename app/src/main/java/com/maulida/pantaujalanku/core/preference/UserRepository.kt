package com.maulida.pantaujalanku.core.preference

class UserRepository (private val sesi : SetPreferences) {
    companion object{
        
        private var INSTANCE : UserRepository? = null
        
        fun getInstance(sesi: SetPreferences) : UserRepository =
                INSTANCE ?: synchronized(this){
                    INSTANCE ?: UserRepository(sesi)
                }
    }
    
    fun loginUser(key : String, value : String){
        sesi.createLoginSession()
        sesi.saveToPreference(key, value)
    }

    fun getUser(key : String) = sesi.getFromPreference(key)

    fun isUserLogin() = sesi.isLogin

    fun logoutUser() = sesi.logOut()
}