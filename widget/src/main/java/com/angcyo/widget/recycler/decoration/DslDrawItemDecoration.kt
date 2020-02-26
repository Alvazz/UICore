package com.angcyo.widget.recycler.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.arrayMapOf
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.drawable.dslGravity
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.library.L
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.atMost
import com.angcyo.widget.base.exactly
import com.angcyo.widget.base.inflate

/**
 * 支持自绘的分割线
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/26
 */

class DslDrawItemDecoration : DslItemDecoration() {

    var onItemOffsets: (DrawItemDecorationParams, Rect) -> Unit = { _, _ ->

    }

    var onItemDraw: (DrawItemDecorationParams) -> Unit = {

    }

    var onItemDrawOver: (DrawItemDecorationParams) -> Unit = {

    }

    override fun onEachItemDoIt(
        canvas: Canvas?,
        parent: RecyclerView,
        state: RecyclerView.State,
        outRect: Rect?,
        beforeViewHolder: DslViewHolder?,
        viewHolder: DslViewHolder,
        afterViewHolder: DslViewHolder?,
        isOverDraw: Boolean
    ) {
        super.onEachItemDoIt(
            canvas,
            parent,
            state,
            outRect,
            beforeViewHolder,
            viewHolder,
            afterViewHolder,
            isOverDraw
        )
        val params = DrawItemDecorationParams(
            canvas,
            parent,
            state,
            outRect,
            beforeViewHolder,
            viewHolder,
            afterViewHolder,
            isOverDraw
        )
        when {
            outRect != null -> onItemOffsets(params, outRect)
            isOverDraw -> onItemDrawOver(params)
            else -> onItemDraw(params)
        }
    }

    /**调用此方法, 进行分割线的配置绘制*/
    fun drawItemDecoration(
        params: DrawItemDecorationParams,
        action: DrawItemDecorationConfig.() -> Unit
    ) {
        if (params.canvas == null) {
            //跳过
            return
        }
        val drawItemDecorationConfig = DrawItemDecorationConfig()
        drawItemDecorationConfig.action()

        val itemView = params.itemView()

        //左上右下
        val decorationRect = Rect()
        onItemOffsets(params, decorationRect)

        //over
        val viewRect = Rect(itemView.left, itemView.top, itemView.right, itemView.bottom)

        if (decorationRect.left > 0) {
            //需要绘制左边的分割线
            _tempDrawRect.set(
                viewRect.left - decorationRect.left,
                viewRect.top,
                viewRect.left,
                viewRect.bottom
            )
            _drawItemDecoration(params, drawItemDecorationConfig, _tempDrawRect)
        }

        if (decorationRect.top > 0) {
            //需要绘制上边的分割线
            _tempDrawRect.set(
                viewRect.left,
                viewRect.top - decorationRect.top,
                viewRect.right,
                viewRect.top
            )
            _drawItemDecoration(params, drawItemDecorationConfig, _tempDrawRect)
        }

        if (decorationRect.right > 0) {
            //需要绘制右边的分割线
            _tempDrawRect.set(
                viewRect.right,
                viewRect.top,
                viewRect.right + decorationRect.right,
                viewRect.bottom
            )
            _drawItemDecoration(params, drawItemDecorationConfig, _tempDrawRect)
        }

        if (decorationRect.bottom > 0) {
            //需要绘制下边的分割线
            _tempDrawRect.set(
                viewRect.left,
                viewRect.top,
                viewRect.right,
                viewRect.bottom + decorationRect.bottom
            )
            _drawItemDecoration(params, drawItemDecorationConfig, _tempDrawRect)
        }

        if (params.isOverDraw) {
            _drawItemDecoration(params, drawItemDecorationConfig, viewRect)
        }
    }

    //缓存
    val _layoutViewCache = arrayMapOf<Int, View>()

    private fun _drawItemDecoration(
        params: DrawItemDecorationParams,
        config: DrawItemDecorationConfig,
        drawRect: Rect
    ) {

        if (params.canvas == null) {
            //跳过
            return
        }

        if (config.drawLayoutId <= 0 && config.drawDrawable == null) {
            L.w("nothing to draw.")
            return
        }

        var width: Int = config.drawWidth
        var height: Int = config.drawHeight

        if (config.drawWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
            width = drawRect.width()
        }
        if (config.drawHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            height = drawRect.height()
        }

        //先绘制drawDrawable
        config.drawDrawable?.run {
            if (config.drawWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
                width = minimumWidth
            }
            if (config.drawHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                height = minimumHeight
            }

            dslGravity(
                drawRect,
                config.drawGravity,
                width.toFloat(),
                height.toFloat(),
                config.drawOffsetX,
                config.drawOffsetY
            ) { dslGravity, _, _ ->
                setBounds(
                    dslGravity._gravityLeft,
                    dslGravity._gravityTop,
                    dslGravity._gravityRight,
                    dslGravity._gravityBottom
                )
                draw(params.canvas!!)
            }
        }

        //绘制布局
        if (config.drawLayoutId > 0) {
            var layoutView: View? = null

            val widthMeasureSpec = when (config.drawWidth) {
                ViewGroup.LayoutParams.WRAP_CONTENT -> atMost(params.parent.measuredWidth)
                ViewGroup.LayoutParams.MATCH_PARENT -> exactly(width)
                else -> exactly(config.drawWidth)
            }

            val heightMeasureSpec = when (config.drawHeight) {
                ViewGroup.LayoutParams.WRAP_CONTENT -> atMost(params.parent.measuredHeight)
                ViewGroup.LayoutParams.MATCH_PARENT -> exactly(height)
                else -> exactly(config.drawWidth)
            }

            if (config.drawLayoutCache) {
                layoutView = _layoutViewCache[config.drawLayoutId]
            }
            if (layoutView == null) {
                layoutView = params.parent.inflate(config.drawLayoutId, false)
                if (config.drawLayoutCache) {
                    _layoutViewCache[config.drawLayoutId] = layoutView
                }
            }
            config.onInitLayout(layoutView)
            layoutView.measure(widthMeasureSpec, heightMeasureSpec)
            layoutView.layout(0, 0, layoutView.measuredWidth, layoutView.measuredHeight)

            dslGravity(
                drawRect,
                config.drawGravity,
                layoutView.measuredWidth.toFloat(),
                layoutView.measuredHeight.toFloat(),
                config.drawOffsetX,
                config.drawOffsetY
            ) { dslGravity, _, _ ->
                params.canvas?.apply {
                    save()
                    translate(dslGravity._gravityLeft.toFloat(), dslGravity._gravityTop.toFloat())
                    layoutView.draw(this)
                    restore()
                }
            }
        }
    }
}

data class DrawItemDecorationParams(
    var canvas: Canvas? = null,
    var parent: RecyclerView,
    var state: RecyclerView.State,
    var outRect: Rect? = null,
    var beforeViewHolder: DslViewHolder? = null,
    var viewHolder: DslViewHolder,
    var afterViewHolder: DslViewHolder? = null,
    var isOverDraw: Boolean = false
)

data class DrawItemDecorationConfig(

    //需要绘制的布局
    @LayoutRes
    var drawLayoutId: Int = -1,
    //是否使用布局缓存,开启后将只会进行一次[inflate]操作
    var drawLayoutCache: Boolean = true,
    //布局初始化
    var onInitLayout: (View) -> Unit = {},

    //需要绘制的Drawable
    var drawDrawable: Drawable? = null,

    //绘制测量的宽高
    var drawWidth: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    var drawHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT,

    //绘制偏移
    var drawOffsetX: Int = 0,
    var drawOffsetY: Int = 0,

    //相对于ItemView的Gravity
    var drawGravity: Int = Gravity.CENTER

)

/**是否在界面中的第一个位置*/
fun DrawItemDecorationParams.isLayoutFirst() = beforeViewHolder == null

/**是否在界面中的最后一个位置*/
fun DrawItemDecorationParams.isLayoutLast() = afterViewHolder == null

/**和前一个item的类型不同*/
fun DrawItemDecorationParams.isDifferentBefore() =
    beforeViewHolder?.itemViewType != viewHolder.itemViewType

/**和后一个item的类型不同*/
fun DrawItemDecorationParams.isDifferentAfter() =
    afterViewHolder?.itemViewType != viewHolder.itemViewType

/**和前后item的类型不同*/
fun DrawItemDecorationParams.isDifferentBoth() = isDifferentBefore() && isDifferentAfter()

fun DrawItemDecorationParams.itemPosition() = viewHolder.adapterPosition
fun DrawItemDecorationParams.itemView() = viewHolder.itemView

fun dslDrawItemDecoration(
    recyclerView: RecyclerView,
    action: DslDrawItemDecoration.() -> Unit
): DslDrawItemDecoration {
    return DslDrawItemDecoration().apply {
        action()
        attachToRecyclerView(recyclerView)
    }
}