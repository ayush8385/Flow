package com.ayush.flow.Services

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceUtils {
    val TAG = SharedPreferenceUtils::class.java.simpleName
    val NIGHT_MODE = "night_mode"
    val IS_LOGGED = "isLoggedIn"
    val IS_PRIVATE = "isPrivate"
    val MY_NAME = "name"
    val MY_USERID = "uid"
    val EMAIL= "email"
    val NUMBER = "number"
    val PASSWORD = "password"
    val ABOUT = "about"
    val MY_PROFILE_URL = "profile"
    val HIDE_PASS = "passcode"
    val APP_KEY = "app_key"
    val APP_SECRET = "app_secret"
    private var mSharedPref: SharedPreferences? = null


    fun getStringPreference(key: String?, defaultValue: String?): String? {
        return mSharedPref!!.getString(key, defaultValue)
    }

    fun removeStringPreference(key: String?) {
        if (key == null || key.isEmpty() || mSharedPref == null) {
            return
        }
        val editor = mSharedPref!!.edit()
        editor.remove(key)
        editor.apply()
    }

    fun setStringPreference(key: String?, value: String?) {
        if (key == null || key.isEmpty() || mSharedPref == null) {
            return
        }
        val editor = mSharedPref!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getIntPreference(key: String?, defaultValue: Int): Int {
        return mSharedPref!!.getInt(key, defaultValue)
    }

    fun setIntPreference(key: String?, value: Int) {
        if (key == null || key.isEmpty() || mSharedPref == null) {
            return
        }
        val editor = mSharedPref!!.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getLongPreference(key: String?, defaultValue: Long): Long {
        return mSharedPref!!.getLong(key, defaultValue)
    }

    fun setLongPreference(key: String?, value: Long) {
        if (key == null || key.isEmpty() || mSharedPref == null) {
            return
        }
        val editor = mSharedPref!!.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getBooleanPreference(key: String?, defaultValue: Boolean): Boolean {
        return mSharedPref!!.getBoolean(key, defaultValue)
    }

    fun setBooleanPreference(key: String?, value: Boolean) {
        if (key == null || key.isEmpty() || mSharedPref == null) {
            return
        }
        val editor = mSharedPref!!.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }


    fun init(context: Context) {
        if (mSharedPref == null) {
            synchronized(SharedPreferenceUtils::class.java) {
                if (mSharedPref == null) {
                    mSharedPref = context.getSharedPreferences(
                        Constants.APP_SHARED_PREFERENCE,
                        Context.MODE_PRIVATE
                    )
                }
            }
        }
    }
}