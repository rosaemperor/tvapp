package com.qubuxing.qbx.http.beans

class SmsRecord constructor(contactName : String , phoneNumber : String ,
                            content : String , time : String , type : String) {
    var contactName : String = contactName
    var phoneNumber : String = phoneNumber
    var content : String = content
    var time :String = time
    var type : String = type

}