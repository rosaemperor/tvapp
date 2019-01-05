package com.qubuxing.qbx.utils

import android.content.Context
import android.content.SharedPreferences
import com.qubuxing.qbx.QBXApplication


class SharePrefenceHelper {
    companion object {
        var mContext : Context ?= null
        var preferences :SharedPreferences ?=null
        fun initSharePreference(context: Context){
            mContext = context
        }
        fun save (key : String , value : String){
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null)preferences= mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)

            var editor :SharedPreferences.Editor  = preferences!!.edit()
            editor.putString(key,value)
            editor.commit()
        }

        fun get(key : String) : String{
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null) mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)
            try {
                return preferences!!.getString(key,"")
            }catch (kot: KotlinNullPointerException){
                return ""
            }
        }
        fun saveBoolean(key : String, value : Boolean){
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null)preferences= mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)

            var editor :SharedPreferences.Editor  = preferences!!.edit()
            editor.putBoolean(key,value)
            editor.commit()
        }
        fun getBoolean(key : String) : Boolean{
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null) mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)
            try {
                return preferences!!.getBoolean(key,false)
            }catch (kot: KotlinNullPointerException){
                return false
            }
        }
        fun saveLong(key : String , value : Long){
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null)preferences= mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)

            var editor :SharedPreferences.Editor  = preferences!!.edit()
            editor.putLong(key,value)
            editor.commit()
        }
        fun getLong(key : String) : Long{
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null) mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)
            try {
                return preferences!!.getLong(key,0)
            }catch (kot: KotlinNullPointerException){
                return 0
            }
        }
        fun saveFloat(key : String , value : Float ){
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null)preferences= mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)
            var editor :SharedPreferences.Editor  = preferences!!.edit()
            editor.putFloat(key,value)
            editor.commit()
        }
        fun getFloat(key : String) : Float{
            if (mContext == null) mContext=QBXApplication.instance.applicationContext
            if (preferences == null) mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)
            try {
                return preferences!!.getFloat(key,0f)
            }catch (kot: KotlinNullPointerException){
                return 0f
            }
        }
    }


}