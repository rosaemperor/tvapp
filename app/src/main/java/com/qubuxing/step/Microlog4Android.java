package com.qubuxing.step;

import android.content.Context;
import android.util.Log;

import com.google.code.microlog4android.LoggerFactory;

/**
 * @author :  jiahongfei
 * @email : jiahongfeinew@163.com
 * @date : 2018/2/6
 * @desc :
 */

public class Microlog4Android {

    private static final com.google.code.microlog4android.Logger logger = LoggerFactory.getLogger();

    public void configure(Context context){
        if(null != logger) {
//            PropertyConfigurator.getConfigurator(context).configure();
//            FileAppender appender = (FileAppender) logger.getAppender(1);
//            appender.setAppend(true);
//            logger.addAppender(appender);
        }
    }

    public void error(Object message){
        if(null != logger) {
//            logger.error(message);
            Log.e("Microlog4Android",""+message.toString());
        }
    }

}
