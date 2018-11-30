package com.qubuxing.qbx.utils

import android.content.Context
import android.os.Build
import android.content.Intent
import android.content.ComponentName
import android.net.Uri
import android.provider.Settings
import java.lang.Exception


class JumpSetting {
    companion object {
        fun getMobileType(): String {
            return Build.MANUFACTURER
        }

        fun jumpStartInterface(context: Context) {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            var componentName: ComponentName? = null
            try {

            if (getMobileType() == "Xiaomi") {
                componentName = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            } else if (getMobileType() == "Letv") {
                intent.action = "com.letv.android.permissionautoboot"
            } else if (getMobileType().equals("samsung")) { // 三星Note5测试通过
                componentName = ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
            } else if (getMobileType().equals("HUAWEI")) { // 华为测试通过
                componentName = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
            } else if (getMobileType().equals("vivo")) { // VIVO测试通过
                componentName = ComponentName.unflattenFromString("com.iqoo.secure/.safeguard.PurviewTabActivity");
            } else if (getMobileType().equals("Meizu")) { //万恶的魅族
                // 通过测试，发现魅族是真恶心，也是够了，之前版本还能查看到关于设置自启动这一界面，系统更新之后，完全找不到了，心里默默Fuck！
                // 针对魅族，我们只能通过魅族内置手机管家去设置自启动，所以我在这里直接跳转到魅族内置手机管家界面，具体结果请看图
                componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity");
            } else if (getMobileType().equals("OPPO")) { // OPPO R8205测试通过
                componentName = ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity");
            } else if (getMobileType().equals("ulong")) { // 360手机 未测试
                componentName = ComponentName("com.yulong.android.coolsafe", ".ui.activity.autorun.AutoRunListActivity");
            } else {
                // 以上只是市面上主流机型，由于公司你懂的，所以很不容易才凑齐以上设备
                // 针对于其他设备，我们只能调整当前系统app查看详情界面
                // 在此根据用户手机当前版本跳转系统设置界面
                if (Build.VERSION.SDK_INT >= 9) {
                    intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS";
                    intent.data = Uri.fromParts("package", context.getPackageName(), null);
                } else if (Build.VERSION.SDK_INT <= 8) {
                    intent.action = Intent.ACTION_VIEW;
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
                }

            }
            intent.component = componentName;
            context.startActivity(intent)

        }catch (e : Exception){
                var intent =Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            }

        }
    }
}