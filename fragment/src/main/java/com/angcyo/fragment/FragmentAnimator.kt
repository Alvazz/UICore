package com.angcyo.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import com.angcyo.library._screenWidth
import com.angcyo.library.ex._integer
import com.angcyo.widget.base.animatorOf

/**
 * [Fragment]切换动画约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object FragmentAnimator {

    /**显示进入动画*/
    var DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_x_show_enter_holder
    //R.anim.lib_translate_x_show_enter //R.anim.lib_x_show_enter_holder

    var DEFAULT_SHOW_EXIT_ANIMATOR = 0
    //R.anim.lib_translate_x_show_exit //R.anim.lib_x_show_exit_holder

    var DEFAULT_REMOVE_ENTER_ANIMATOR = 0
    //R.anim.lib_translate_x_remove_enter //R.anim.lib_x_remove_enter_holder

    /**隐藏时退出动画*/
    var DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_x_remove_exit_holder
    //R.anim.lib_translate_x_remove_exit //R.anim.lib_x_remove_exit_holder

    var loadAnimator: (context: Context, anim: Int) -> Animator? = { context, anim ->
        val sw = _screenWidth.toFloat()
        val duration = _integer(R.integer.lib_animation_duration).toLong()

        val objectAnimator = ObjectAnimator()
        objectAnimator.duration = duration
        objectAnimator.interpolator = AccelerateDecelerateInterpolator()

        /*将占位的动画, 翻译成属性动画*/
        when (anim) {
            R.anim.lib_x_show_enter_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(sw, 0f)
                objectAnimator
            }
            R.anim.lib_x_show_exit_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(0f, -sw * 0.8f)
                objectAnimator.interpolator = AccelerateInterpolator()
                objectAnimator
            }
            R.anim.lib_x_remove_enter_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(-sw, 0f)
                objectAnimator
            }
            R.anim.lib_x_remove_exit_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(0f, sw)
                objectAnimator
            }
            else -> animatorOf(context, anim)
        }
    }
}