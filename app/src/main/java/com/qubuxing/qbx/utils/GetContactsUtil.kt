package com.qubuxing.qbx.utils

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import com.qubuxing.qbx.http.beans.Contact
import java.util.ArrayList

class GetContactsUtil(private val mContext: Context) {
    /**
     * 获取库Phon表字段
     */
    private val PHONES_PROJECTION = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
    /**
     * 联系人显示名称
     */
    private val PHONES_DISPLAY_NAME_INDEX = 0

    /**
     * 电话号码
     */
    private val PHONES_NUMBER_INDEX = 1

    /**
     * 头像ID
     */
    private val PHONES_PHOTO_ID_INDEX = 2

    /**
     * 联系人的ID
     */
    private val PHONES_CONTACT_ID_INDEX = 3
    private val PERMISSIONS_CONTACT = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS)

    /**
     * 联系人名称
     */
    private val mContactsName = ArrayList<String>()

    /**
     * 联系人头像
     */
    private val mContactsNumber = ArrayList<String>()

    /**
     * 联系人头像
     */
    private val mContactsPhonto = ArrayList<Bitmap>()

    val contacts: List<Contact>
        get() {
            val result = phoneContacts
            result.addAll(simContacts)

            return result
        }

    /**
     * 得到手机通讯录联系人信息
     */
    private// 获取手机联系人
    //得到手机号码
    //当手机号码为空的或者为空字段 跳过当前循环
    //得到联系人名称
    val phoneContacts: MutableList<Contact>
        get() {
            val contacts = ArrayList<Contact>()
            val resolver = mContext.contentResolver
            val phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null)


            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    val phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX)
                    if (TextUtils.isEmpty(phoneNumber))
                        continue
                    val contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX)
                    val contact = Contact(contactName, phoneNumber)
                    contacts.add(contact)
                }

                phoneCursor.close()

            }
            return contacts
        }

    /**
     * 得到手机SIM卡联系人人信息
     */
    private// 获取Sims卡联系人
    //判断SIM的读取状态 ，，，0到4 为不可读取状态，5为可读取状态
    // 得到手机号码
    // 当手机号码为空的或者为空字段 跳过当前循环
    // 得到联系人名称
    val simContacts: List<Contact>
        get() {
            val resolver = mContext.contentResolver
            val contacts = ArrayList<Contact>()
            var phoneCursor: Cursor? = null
            val uri = Uri.parse("content://icc/adn")
            val manager = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (manager.simState == TelephonyManager.SIM_STATE_READY) {
                try {
                    phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null, null)
                } catch (exception: SecurityException) {
                    Log.d("SecurityException", "SIM状态异常")
                } catch (e: Exception) {

                }

            }

            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    val phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX)
                    if (TextUtils.isEmpty(phoneNumber))
                        continue
                    val contactName = phoneCursor
                            .getString(PHONES_DISPLAY_NAME_INDEX)

                    val contact = Contact(contactName, phoneNumber)

                }

                phoneCursor.close()
            }
            return contacts
        }

}