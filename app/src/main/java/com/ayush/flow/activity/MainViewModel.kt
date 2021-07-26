package com.ayush.flow.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val mutableLiveData:MutableLiveData<String> = MutableLiveData()

    public fun setText(s:String){
        mutableLiveData.value=s
    }

    public fun getText():MutableLiveData<String>{
        return mutableLiveData
    }
}