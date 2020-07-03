package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.library.ex.simpleHash
import kotlin.random.Random

/**
 * 每个无障碍拦截后需要执行的动作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

abstract class BaseAccessibilityAction {

    /**关联的拦截器*/
    var accessibilityInterceptor: BaseAccessibilityInterceptor? = null

    /**当完成了[Action], 需要调用此方法, 触发下一个[Action]*/
    var actionFinish: ((error: ActionException?) -> Unit)? = null

    /**[doAction]执行后的次数统计*/
    var actionDoCount = 0

    /**用于控制下一次[Action]检查执行的延迟时长, 毫秒. 负数表示使用[Interceptor]的默认值*/
    var actionIntervalDelay: Long = -1

    /**自动在每个[onActionFinish]结束之后, 随机调整[actionIntervalDelay]的时间*/
    var autoIntervalDelay: Boolean = true

    /**是否需要事件[event],返回true表示需要处理*/
    open fun checkEvent(service: BaseAccessibilityService, event: AccessibilityEvent?): Boolean {
        return false
    }

    /**执行action*/
    open fun doAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {

    }

    /**执行action来自其他action不需要处理, 返回true表示处理了事件*/
    open fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        return false
    }

    /**[Action]首次执行开始*/
    @CallSuper
    open fun onActionStart(interceptor: BaseAccessibilityInterceptor) {
        accessibilityInterceptor = interceptor
    }

    /**[Action]执行完成, 可以用于释放一些数据*/
    @CallSuper
    open fun onActionFinish(error: ActionException? = null) {
        if (autoIntervalDelay) {
            onRandomIntervalDelay()
        }
        actionFinish?.invoke(error)
        actionFinish = null
        accessibilityInterceptor = null
        actionDoCount = 0
    }

    /**一个名字*/
    open fun getActionTitle(): String {
        return this.simpleHash()
    }

    /**随机产生一个间隔时间*/
    open fun onRandomIntervalDelay() {
        randomIntervalDelay()
    }
}

/**随机间隔延迟时长*/
fun BaseAccessibilityAction.randomIntervalDelay() {
    actionIntervalDelay =
        (accessibilityInterceptor?.initialIntervalDelay ?: -1) * Random.nextInt(1, 10)
}