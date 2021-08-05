package com.ayush.flow.Notification

class Data {
    private var receiver:String=""
    private var sender:String=""
    private var message:String=""
    private var type:Int=0

    constructor(){}
    constructor(receiver: String,sender: String,message:String,type:Int) {
        this.receiver=receiver
        this.sender=sender
        this.message=message
        this.type=type
    }

    fun getReceiver():String{
        return receiver
    }
    fun setReceiver(receiver: String){
        this.receiver=receiver
    }

    fun getSender():String{
        return sender
    }
    fun setSender(sender: String){
        this.sender=sender
    }

    fun getMessage():String{
        return message
    }
    fun setMessage(message: String){
        this.message=message
    }

    fun getType():Int{
        return type
    }
    fun setType(type:Int){
        this.type=type
    }
}