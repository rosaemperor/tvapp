package com.qubuxing.qbx
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qubuxing.qbx.http.RetrofitUtil


/**
 * baseFragment暂时未使用
 */
abstract class BaseFragment : Fragment(){
    lateinit var httpHelp : HttpService
    lateinit var baseBinding: ViewDataBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        httpHelp = RetrofitUtil.instance.help
//        baseBinding  = DataBindingUtil.inflate(inflater , getLayoutID() , container , false ,DataBindingUtil.getDefaultComponent())
        return baseBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }


    abstract fun initViewModel()

    abstract fun getLayoutID() : Int


    fun findBinding() : ViewDataBinding{
        return baseBinding
    }

}
