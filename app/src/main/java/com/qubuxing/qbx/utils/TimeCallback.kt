package com.qubuxing.qbx.utils

import java.io.File

interface TimeCallback {
     fun currentTime(currenTime: Long)

    fun failuredGetTime()
}