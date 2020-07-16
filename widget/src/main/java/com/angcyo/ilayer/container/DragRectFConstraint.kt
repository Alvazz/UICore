package com.angcyo.ilayer.container

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.RectF
import android.view.animation.LinearInterpolator
import com.angcyo.drawable.isLeft
import com.angcyo.drawable.isTop
import com.angcyo.ilayer.ILayer
import com.angcyo.library.ex.abs
import kotlin.math.max

/**
 * 约束4个边距离边界的比例. 那么多拖拽结束后, 就会被限制在这个边界上.
 * 取值小于等于0, 表示强制约束比例到此值.
 * 取值大于0, 表示最小约束比例到此值. 也就是 到达对应边界距离的比例至少是此值
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DragRectFConstraint(val rectF: RectF) : IDragConstraint {

    var interpolator: TimeInterpolator? = LinearInterpolator() //BounceInterpolator()
    var duration = 240L

    var _valueAnimator: ValueAnimator? = null

    override fun onDragMoveTo(container: IContainer, layer: ILayer, gravity: Int, x: Int, y: Int) {
        _valueAnimator?.cancel()
    }

    override fun onDragEnd(container: IContainer, layer: ILayer, position: OffsetPosition) {
        _valueAnimator?.cancel()

        val endX: Float = if (position.gravity.isLeft()) {
            if (rectF.left <= 0) {
                rectF.left.abs()
            } else {
                max(rectF.left, position.offsetX)
            }
        } else {
            if (rectF.right <= 0) {
                rectF.right.abs()
            } else {
                max(rectF.right, position.offsetX)
            }
        }

        val endY: Float = if (position.gravity.isTop()) {
            if (rectF.top <= 0) {
                rectF.top.abs()
            } else {
                max(rectF.top, position.offsetY)
            }
        } else {
            if (rectF.bottom <= 0) {
                rectF.bottom.abs()
            } else {
                max(rectF.bottom, position.offsetY)
            }
        }

        _valueAnimator = ObjectAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                val x = position.offsetX + (endX - position.offsetX) * value
                val y = position.offsetY + (endY - position.offsetY) * value
                container.update(layer, OffsetPosition(position.gravity, x, y))
            }
            duration = this@DragRectFConstraint.duration
            interpolator = this@DragRectFConstraint.interpolator

            start()
        }
    }
}