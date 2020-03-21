package com.angcyo.behavior.linkage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.NestedScrollingChild
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.widget.base.*
import kotlin.math.abs
import kotlin.math.min

/**
 * 头/悬浮/尾 联动滚动, 头部的行为
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class LinkageHeaderBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseLinkageBehavior(context, attributeSet) {

    val minScroll: Int
        get() {
            var result = -min(headerView.mH(), footerView.mH() + stickyView.mH())

            if (fixTitleBar && titleBarBehavior != null) {
                result += titleBarBehavior?.getContentExcludeHeight(this) ?: 0
            } else if (fixStatusBar) {
                result += childView?.getStatusBarHeight() ?: 0
            }
            result += fixScrollTopOffset
            return result
        }

    val maxScroll: Int
        get() = 0

    /**不管Footer是否可以滚动, 都优先滚动Header*/
    var priorityHeader = false //优先滚动头部

    /**滚动最小值, 要考虑标题栏的高度*/
    var fixTitleBar: Boolean = true

    /**滚动最小值, 要考虑状态栏的高度*/
    var fixStatusBar: Boolean = true

    /**滚动最小值, 额外要考虑的距离*/
    var fixScrollTopOffset: Int = 0

    /**标题栏*/
    var titleBarBehavior: ITitleBarBehavior? = null

    init {
        showLog = false
        onScrollTo = { x, y ->
            //L.w("scrollTo:$y")
            childView?.offsetTopTo(y)
        }
    }

    //<editor-fold desc="内嵌滚动处理">

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        headerView = child

        dependency.behavior()?.apply {
            if (this is ITitleBarBehavior) {
                titleBarBehavior = this
            }
        }

        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (target == footerScrollView) {
            //如果是底部传来的内嵌滚动
            if (priorityHeader || (scrollY != minScroll && scrollY != maxScroll) /*防止头部滚动一半的情况*/) {
                consumedScrollVertical(dy, scrollY, minScroll, maxScroll, consumed)
            } else {
                //这里处理Footer不能滚动时, 再滚动
                if (dy > 0 && scrollY != maxScroll) {
                    //手指向上滑动
                    consumedScrollVertical(dy, scrollY, minScroll, maxScroll, consumed)
                } else if (scrollY != minScroll) {
                    consumedScrollVertical(dy, scrollY, minScroll, maxScroll, consumed)
                }
            }
        } else if (scrollY != 0 && target == headerScrollView) {
            //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
            consumedScrollVertical(dy, consumed)
        } else {
            //其他位置发生的内嵌滚动, 比如 Sticky
            onNestedPreScrollOther(dy, consumed)
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        onHeaderOverScroll(-dyUnconsumed)
    }

    //</editor-fold desc="内嵌滚动处理">

    //<editor-fold desc="其他滚动处理">

    /**其他位置发生的内嵌滚动处理, 比如Sticky*/
    fun onNestedPreScrollOther(dy: Int, consumed: IntArray) {
        //当无内嵌滚动的view访问, 此时发生了滚动的情况下.
        //优先处理footer滚动, 其次处理header滚动
        val nestedScrollingChild = footerScrollView
        val footerView: View? = if (nestedScrollingChild is View) {
            nestedScrollingChild
        } else {
            null
        }

        if (dy > 0) {
            //手指向上滑动
            consumedScrollVertical(
                dy,
                scrollY,
                minScroll,
                maxScroll,
                consumed
            )
            if (consumed[1] == 0) {
                //不需要消耗了
                footerView?.scrollBy(0, dy)
            }
        } else {
            //手指向下滚动
            if (footerView.topCanScroll()) {
                footerView?.scrollBy(0, dy)
                consumed[1] = dy
            } else {
                if (_nestedScrollView == null) {
                    //没有内嵌滚动访问, Touch事件导致的滑动, 就偏移Header
                    onHeaderOverScroll(-dy)
                }
            }
        }
    }

    /**头部到达边界的滚动处理*/
    fun onHeaderOverScroll(dy: Int) {
        val scroll = MathUtils.clamp(scrollY + dy, minScroll, maxScroll)
        scrollTo(scrollX, scroll)
    }

    //</editor-fold desc="其他滚动处理">

    //<editor-fold desc="非内嵌滚动处理">

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val absX = abs(velocityX)
        val absY = abs(velocityY)
        if (_nestedScrollView == null &&
            _flingScrollView == null &&
            absY > absX && absY > minFlingVelocity
        ) {
            val delegateScrollView: NestedScrollingChild? = footerScrollView ?: headerScrollView
            delegateScrollView?.apply {
                setFlingView(this)
                val vY = -velocityY.toInt()
                (footerView ?: headerView)?.behavior()?.apply {
                    if (this is LinkageFooterBehavior) {
                        //这一点很重要, 因为是模拟出来的fling操作
                        this._nestedPreFling = true
                        this._nestedFlingDirection = vY
                    }
                }
                fling(0, velocity(vY))
            }
            //L.i("fling $velocityY")
            return true
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    val _scrollConsumed = intArrayOf(0, 0)

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val absX = abs(distanceX)
        val absY = abs(distanceY)

        if (_nestedScrollView == null) {
            stopScrollAndFling()
        }

        if (_nestedScrollView == null && absY > absX && absY > touchSlop && e1 != null && e2 != null) {
            //L.i("scroll $distanceY")
            onNestedPreScrollOther(distanceY.toInt(), _scrollConsumed)
            return true
        }
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    //</editor-fold desc="非内嵌滚动处理">

}