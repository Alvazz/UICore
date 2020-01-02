package com.angcyo.widget

import androidx.annotation.IdRes
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.angcyo.tablayout.DslTabLayout

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun DslViewHolder.vp(@IdRes id: Int): ViewPager? {
    return v(id)
}

fun DslViewHolder.vp2(@IdRes id: Int): ViewPager2? {
    return v(id)
}

fun DslViewHolder.tab(@IdRes id: Int): DslTabLayout? {
    return v(id)
}

fun DslViewHolder.button(@IdRes id: Int): DslButton? {
    return v(id)
}