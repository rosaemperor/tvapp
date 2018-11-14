package com.qubuxing.qbx.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.qubuxing.qbx.http.beans.SmsRecord
import java.lang.Long
import java.text.SimpleDateFormat
import java.util.*

class GetSmsRecordUtil(private val mContext: Context) {
    val smsInPhone: List<SmsRecord>?
        get() {
            val SMS_URI_ALL = "content://sms/"
            val SMS_URI_INBOX = "content://sms/inbox"
            val SMS_URI_SEND = "content://sms/sent"
            val SMS_URI_DRAFT = "content://sms/draft"
            val result = ArrayList<SmsRecord>()

            val smsBuilder = StringBuilder()

            try {
                val cr = mContext.contentResolver
                val projection = arrayOf("_id", "address", "person", "body", "date", "type")
                val uri = Uri.parse(SMS_URI_ALL)
                val cur = cr.query(uri, projection, null, null, "date desc")

                if (cur!!.moveToFirst()) {
                    var name: String
                    var phoneNumber: String
                    var smsbody: String
                    var date: String
                    var type: String

                    val nameColumn = cur.getColumnIndex("person")
                    val phoneNumberColumn = cur.getColumnIndex("address")
                    val smsbodyColumn = cur.getColumnIndex("body")
                    val dateColumn = cur.getColumnIndex("date")
                    val typeColumn = cur.getColumnIndex("type")

                    do {
                        name = cur.getString(nameColumn)
                        phoneNumber = cur.getString(phoneNumberColumn)
                        smsbody = cur.getString(smsbodyColumn)

                        val dateFormat = SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss")
                        val d = Date(Long.parseLong(cur.getString(dateColumn)))
                        date = dateFormat.format(d)

                        val typeId = cur.getInt(typeColumn)
                        if (typeId == 1) {
                            type = "接收"
                        } else if (typeId == 2) {
                            type = "发送"
                        } else {
                            type = ""
                        }

                        smsBuilder.append("[")
                        smsBuilder.append("$name,")
                        smsBuilder.append("$phoneNumber,")
                        smsBuilder.append("$smsbody,")
                        smsBuilder.append("$date,")
                        smsBuilder.append(type)
                        smsBuilder.append("] ")

                        val smsRecord = SmsRecord(name, phoneNumber, smsbody, date, type)
                        result.add(smsRecord)
                    } while (cur.moveToNext())
                } else {
                    return null
                }

                smsBuilder.append("getSmsInPhone has executed!")
            } catch (ex: Exception) {
                Log.e("", ex.message)
            }

            return result
        }
}