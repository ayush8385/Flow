package com.ayush.flow.model

class ScreenItem {
    var title:String
    var desc:String
    var image:Int = 0


    constructor(title: String, desc: String, image: Int) {
        this.title = title
        this.desc = desc
        this.image = image
    }

    @JvmName("getTitle1")
    fun getTitle(): String {
        return this.title
    }

    @JvmName("setTitle1")
    fun setTitle(title: String){
        this.title=title
    }

    @JvmName("getDesc1")
    fun getDesc():String{
        return this.desc
    }

    @JvmName("setDesc1")
    fun setDesc(desc: String){
        this.desc=desc
    }

    @JvmName("getImage1")
    fun getImage():Int{
        return this.image
    }

    @JvmName("setImage1")
    fun setImage(image:Int){
        this.image=image
    }


}