package com.angcyo.core.component.accessibility.base

import android.graphics.Color
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.Action
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.openApp
import com.angcyo.library.getAppName
import com.angcyo.widget.span.span


/**
 * 无障碍悬浮窗 任务状态提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityWindow {

    /**触发的保存窗口日志*/
    var onSaveWindowLog: ((log: String) -> Unit)? = null

    var showCatchButton: Boolean = isDebug()
        set(value) {
            val old = field
            field = value
            if (old != value && value) {
                AccessibilityWindowFullLayer.update()
                AccessibilityWindowMiniLayer.update()
            }
        }

    val _defaultClickAction: Action = {
        app().openApp()
    }

    /**点击浮窗的回调*/
    var onLayerClickAction: Action? = _defaultClickAction

    var onStopAction: Action? = null

    var fullscreenLayer: Boolean = false
        set(value) {
            field = value
            if (value) {
                AccessibilityWindowMiniLayer.hide()
                if (!_isNeedHide()) {
                    AccessibilityWindowFullLayer._show()
                }
            } else {
                AccessibilityWindowFullLayer.hide()
                if (!_isNeedHide()) {
                    AccessibilityWindowMiniLayer._show()
                }
            }
        }

    var fullTopText: CharSequence? = span {
        append(getAppName())
        appendln()
        append("全自动操作中...")
    }

    var fullTitleText: CharSequence? = span {
        append("任务进行中")
    }

    /**步骤进度提示文本*/
    var text: CharSequence? = null

    var textColor: Int = Color.WHITE

    /**描述文本*/
    var des: CharSequence? = null

    /**描述概要文本*/
    var summary: CharSequence? = null

    /**转圈时长, 毫秒. -1 保持原来的进度; 0 清空进度; 其他 进度动画时长*/
    var duration: Long = -1

    /**浮窗需要隐藏到什么时间, 13位时间戳*/
    var _hideToTime: Long = -1

    //隐藏时长
    var _hideTime: Long = -1

    /**还需要隐藏的次数, >0生效*/
    var _hideToCount: Long = -1

    //显示浮窗
    val _showRunnable: Runnable? = Runnable {
        show()
    }

    /**清空默认*/
    fun reset() {
        this.text = null
        this.summary = null
        this.des = null
        this.duration = 0
        this.textColor = Color.WHITE
    }

    fun show(dsl: AccessibilityWindow.() -> Unit = {}) {
        dsl()
        if (!_isNeedHide()) {
            _showRunnable?.let {
                MainExecutor.handler.removeCallbacks(it)
            }

            if (fullscreenLayer) {
                AccessibilityWindowFullLayer.show()
            } else {
                AccessibilityWindowMiniLayer.show()
            }

            //clear
            _hideTime = -1
        }
    }

    fun update() {
        AccessibilityWindowFullLayer.update()
        AccessibilityWindowMiniLayer.update()
    }

    fun hide() {
        AccessibilityWindowMiniLayer.hide()
        AccessibilityWindowFullLayer.hide()
    }

    fun _isNeedHide(): Boolean {
        return if (_hideToCount > 0) {
            //需要隐藏
            true
        } else {
            val nowTime: Long = nowTime()
            nowTime <= _hideToTime
        }
    }

    /**异常多少次的显示请求*/
    fun hideCount(count: Long) {
        _hideToCount = count
        if (count > 0) {
            hide()
        }
    }

    fun hideCountDown() {
        _hideToCount--
    }

    /**浮窗隐藏多长时间*/
    fun hideTime(time: Long) {
        _hideToCount = -1

        _showRunnable?.let {
            MainExecutor.handler.removeCallbacks(it)
        }

        if (time > 0) {
            hide()

            _hideTime = time
            _hideToTime = time + nowTime()

            _showRunnable?.let {
                MainExecutor.handler.postDelayed(it, time)
            }
        } else {
            _hideTime = -1
            _hideToTime = -1
        }
    }
}