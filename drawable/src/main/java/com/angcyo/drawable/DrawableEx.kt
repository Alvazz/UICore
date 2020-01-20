package com.angcyo.drawable

import android.content.res.Resources
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.angcyo.library.ex.undefined_int

/**
 * Created by angcyo on ：2017/12/15 15:01
 * 修改备注：
 * Version: 1.0.0
 */

/**将Drawable放在inRect的指定点centerPoint的位置*/
fun Drawable.getBoundsWith(centerPoint: Point, inRect: Rect) = Rect().apply {
    left = inRect.left + centerPoint.x - intrinsicWidth / 2
    top = inRect.top + centerPoint.y - intrinsicHeight / 2
    right = left + centerPoint.x + intrinsicWidth / 2
    bottom = top + centerPoint.y + intrinsicHeight / 2
}

fun Drawable?.color(filterColor: Int) = this?.run { filterDrawable(filterColor) }

/**
 * 颜色过滤
 */
fun Drawable?.colorFilter(@ColorInt color: Int): Drawable? {
    if (this == null) {
        return null
    }
    val wrappedDrawable = DrawableCompat.wrap(this).mutate()
    wrappedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    return wrappedDrawable
}

/**
 * tint颜色
 */
fun Drawable?.tintDrawable(@ColorInt color: Int): Drawable? {
    if (this == null) {
        return null
    }
    val wrappedDrawable = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(wrappedDrawable, color)
    return wrappedDrawable
}

/**
 * 根据版本, 自动选择方法
 */
fun Drawable?.filterDrawable(@ColorInt color: Int): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this?.tintDrawable(color)
    } else {
        this?.colorFilter(color)
    }
}

fun Drawable?.copy(res: Resources? = null) = this?.mutate()?.constantState?.newDrawable(res)

fun Drawable?.tintDrawableColor(color: Int): Drawable? {

    if (this == null) {
        return this
    }

    val wrappedDrawable =
        DrawableCompat.wrap(this).mutate()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.setTint(wrappedDrawable, color)
    } else {
        wrappedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    return wrappedDrawable
}

/**初始化bounds*/
fun Drawable.initBounds(width: Int = undefined_int, height: Int = undefined_int): Drawable {
    if (bounds.isEmpty) {
        val w = if (width == undefined_int) minimumWidth else width
        val h = if (height == undefined_int) minimumHeight else height
        bounds.set(0, 0, w, h)
    }
    return this
}