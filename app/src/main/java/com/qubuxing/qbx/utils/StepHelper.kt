package com.qubuxing.qbx.utils

object StepHelper {
    /**
     * 在切换用户时（登录行为）调用，清空当前lastUpdateStep即可
     */
    fun clearStepHistory(){
        SharePrefenceHelper.saveFloat("LastUpdateStep",0f)
    }

}