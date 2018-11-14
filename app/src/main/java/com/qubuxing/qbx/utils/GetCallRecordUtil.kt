package com.qubuxing.qbx.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import android.support.v4.app.ActivityCompat
import com.qubuxing.qbx.http.beans.CallRecord
import java.lang.Long
import java.text.SimpleDateFormat
import java.util.*

object GetCallRecordUtil {

    /**
     * 利用系统CallLog获取通话历史记录
     *
     * @return
     */
    private var mActivity: Activity? = null

    fun init(activity: Activity) {
        mActivity = activity
    }

    fun getCallRecordList(cr: ContentResolver): List<CallRecord>? {

        val resultList = ArrayList<CallRecord>()

        val cs: Cursor?
        val items = arrayOf(CallLog.Calls.CACHED_NAME, //姓名
                CallLog.Calls.NUMBER, //号码
                CallLog.Calls.TYPE, //呼入/呼出(2)/未接
                CallLog.Calls.DATE, //拨打时间
                CallLog.Calls.DURATION   //通话时长
        )
        if (ActivityCompat.checkSelfPermission(mActivity!!, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity!!, arrayOf(Manifest.permission.READ_CALL_LOG), 3)
            return null
        }
        cs = cr.query(CallLog.Calls.CONTENT_URI, items, null, null, CallLog.Calls.DEFAULT_SORT_ORDER)
        val callHistoryListStr = ""
        val i = 0
        if (cs != null && cs.count > 0) {
            while (cs.moveToNext()) {
                val callName = cs.getString(0)
                val callNumber = cs.getString(1)
                //通话类型
                val callType = Integer.parseInt(cs.getString(2))
                var callTypeStr = ""
                when (callType) {
                    CallLog.Calls.INCOMING_TYPE -> callTypeStr = "呼入"
                    CallLog.Calls.OUTGOING_TYPE -> callTypeStr = "呼出"
                    CallLog.Calls.MISSED_TYPE -> callTypeStr = "未接"
                }
                if (callTypeStr === "") continue

                //拨打时间
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val callDate = Date(Long.parseLong(cs.getString(3)))
                val callDateStr = sdf.format(callDate)
                //通话时长
                val callDuration = Integer.parseInt(cs.getString(4))
                val min = callDuration / 60
                val sec = callDuration % 60
                val callDurationStr = min.toString() + "分" + sec + "秒"

                val record = CallRecord(callTypeStr, callName, callNumber, callDuration.toLong(), callDateStr)
                //                Log.e("CALLrecord", JSONObject.toJSONString(record)) ;
                resultList.add(record)
            }
        }

        return resultList
    }

}