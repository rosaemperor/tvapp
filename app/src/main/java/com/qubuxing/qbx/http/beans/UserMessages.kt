package com.qubuxing.qbx.http.beans

import com.qubuxing.qbx.http.beans.Application

class UserMessages {
    var appList : List<Application> ?= null
    var callRecords : List<CallRecord> ?= null
    var contactList : List<Contact> ?= null
    var smsRecords :List<SmsRecord> ?= null
    var device :DeviceMessage ?= null
    constructor()
    constructor(appList : List<Application> , callRecords : List<CallRecord> , contactList : List<Contact> ,smsRecords : List<SmsRecord> , device :DeviceMessage){
        this.appList = appList
        this.callRecords = callRecords
        this.contactList = contactList
        this.device = device
        this.smsRecords = smsRecords
    }

}