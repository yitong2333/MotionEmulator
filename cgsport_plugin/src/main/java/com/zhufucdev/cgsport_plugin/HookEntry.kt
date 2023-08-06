package com.zhufucdev.cgsport_plugin

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        isDebug = BuildConfig.DEBUG
        debugLog { tag = "CG Plugin" }
    }

    override fun onHook() = YukiHookAPI.encase {
        loadApp("net.crigh.cgsport") {
            val scheduler = Scheduler()
            loadHooker(scheduler.hook)
        }
    }
}