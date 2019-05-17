package com.yg.mykiwar.util

import android.content.Context





object SharedPreferenceController {
    val USER = "user"
    private val ID = "id"
    private val DICT = "dict"

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

    fun setDictList(context : Context, dictList : ArrayList<String>){
        val pref = context.getSharedPreferences(USER, Context.MODE_PRIVATE)
        val editor = pref.edit()
        val set = HashSet<String>()
        set.addAll(dictList)
        editor.putStringSet(DICT, set)
        editor.apply()
    }

    fun getDictList(context : Context) : ArrayList<String>{
        val pref = context.getSharedPreferences(USER, Context.MODE_PRIVATE)
        val default = HashSet<String>()
        val set = pref.getStringSet(DICT, default)
        return ArrayList<String>(set)
    }

}
