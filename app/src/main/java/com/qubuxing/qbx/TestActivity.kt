package com.qubuxing.qbx

import android.databinding.DataBindingUtil
import android.os.SystemClock
import com.qubuxing.qbx.databinding.TestLayoutBinding
import com.qubuxing.qbx.http.beans.StepGetEvent
import com.qubuxing.qbx.utils.SharePrefenceHelper
import com.qubuxing.qbx.viewModels.TestViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TestActivity : BaseActivity(){
    lateinit var viewModel : TestViewModel
    lateinit var testBinding : TestLayoutBinding
    override fun initBinding() {
      testBinding = DataBindingUtil.setContentView(this@TestActivity,R.layout.test_layout)

    }

    override fun initViewModel() {
        viewModel = TestViewModel()
        viewModel.setLifecycle(lifecycle)
        testBinding.viewModel = viewModel

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stepGetEvent(event : StepGetEvent){
        var diff = 0.0f


    }
}