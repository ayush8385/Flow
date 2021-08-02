package com.ayush.flow.Services

class Data {
    private var messageid:String=""
    private var user:String=""
    private var icon=0
    private var image:String=""
    private var body:String=""
    private var title:String=""
    private var sent:String=""

    constructor(){}
    constructor(messageid:String,user: String, icon: Int,image:String, body: String, title: String, sent: String) {
        this.messageid=messageid
        this.user = user
        this.icon = icon
        this.image=image
        this.body = body
        this.title = title
        this.sent = sent
    }


    fun getMid():String{
        return messageid
    }
    fun setMid(messageid: String){
        this.messageid=messageid
    }

    fun getUser():String{
        return user
    }
    fun setUser(user:String){
        this.user=user
    }

    fun getBody():String{
        return body
    }
    fun setBody(body:String){
        this.body=body
    }

    fun getImage():String{
        return image
    }
    fun setImage(image:String){
        this.image=image
    }

    fun getIcon():Int{
        return icon
    }
    fun setIcon(icon:Int){
        this.icon=icon
    }

    fun getTitle():String{
        return title
    }
    fun setTitle(title:String){
        this.title=title
    }

    fun getSent():String{
        return sent
    }
    fun setSent(sent:String){
        this.sent=sent
    }
}