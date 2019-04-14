package com.yg.mykiwar.util

import android.content.Context

object SharedPreferenceController {
    val USER = "user"
    private val ID = "id"

    fun setId(context : Context, id : String, name : String){
        val pref = context.getSharedPreferences(USER, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(name, id)
        editor.apply()
    }

    fun getId(context : Context, name : String) : String{
        val pref = context.getSharedPreferences(USER, Context.MODE_PRIVATE)
        return pref.getString(name, "")!!
    }

    fun setLists(context : Context, id : String, name : String){
        val pref = context.getSharedPreferences(USER, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(name, id)
        editor.apply()
    }

    fun getLists(context : Context, name : String) : String {
        val pref = context.getSharedPreferences(USER, Context.MODE_PRIVATE)
        return pref.getString(name, "")!!
    }
}
