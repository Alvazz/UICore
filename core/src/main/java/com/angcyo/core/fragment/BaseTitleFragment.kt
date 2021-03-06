package com.angcyo.core.fragment

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import com.angcyo.DslAHelper
import com.angcyo.base.getAllValidityFragment
import com.angcyo.behavior.placeholder.TitleBarPlaceholderBehavior
import com.angcyo.behavior.refresh.IRefreshBehavior
import com.angcyo.behavior.refresh.IRefreshContentBehavior
import com.angcyo.behavior.refresh.RefreshContentBehavior
import com.angcyo.behavior.refresh.RefreshEffectBehavior
import com.angcyo.component.DslAffect
import com.angcyo.component.dslAffect
import com.angcyo.core.R
import com.angcyo.core.appendTextItem
import com.angcyo.core.behavior.ArcLoadingHeaderBehavior
import com.angcyo.library.component.dslIntent
import com.angcyo.library.ex.className
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.undefined_res
import com.angcyo.lifecycle.onStart
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.*
import com.angcyo.widget.layout.DslSoftInputLayout
import com.angcyo.widget.layout.OnSoftInputListener
import com.angcyo.widget.layout.isHideAction
import com.angcyo.widget.layout.isShowAction
import com.angcyo.widget.span.span
import com.angcyo.widget.text.DslTextView

/**
 * 统一标题管理的Fragment,
 *
 * 界面xml中, 已经有打底的RecycleView.
 *
 * 可以直接通过相关id, replace对应的布局结构
 *
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/07
 */
abstract class BaseTitleFragment : BaseFragment(), OnSoftInputListener {

    //<editor-fold desc="成员配置">

    /**自定义内容布局*/
    var contentLayoutId: Int = -1

    /**自定义内容覆盖布局*/
    var contentOverlayLayoutId: Int = -1

    /**自定义的刷新头部*/
    var refreshLayoutId: Int = -1

    /**自定义标题栏布局*/
    var titleLayoutId: Int = -1

    /**是否激活刷新回调*/
    var enableRefresh: Boolean = false

    /**激活软键盘输入*/
    var enableSoftInput: Boolean = false

    /**是否需要强制显示返回按钮, 否则智能判断*/
    var enableBackItem: Boolean = false

    /**用于控制刷新状态, 开始刷新/结束刷新*/
    var refreshContentBehavior: IRefreshContentBehavior? = null

    /**情感图状态切换, 按需初始化.*/
    var affectUI: DslAffect? = null

    //</editor-fold desc="成员配置">

    //<editor-fold desc="操作属性">

    /**标题*/
    open var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            if (isAdded) {
                _vh.tv(R.id.lib_title_text_view)?.text = value
            }
        }

    init {
        /**Fragment根布局*/
        fragmentLayoutId = R.layout.lib_title_fragment

        fragmentTitle = this.javaClass.simpleName
    }

    //</editor-fold desc="操作属性">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentUI?.fragmentCreateAfter?.invoke(this, fragmentConfig)
    }

    override fun onCreateRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateRootView(inflater, container, savedInstanceState)
        if (enableSoftInput) {
            val softInputLayout = DslSoftInputLayout(fContext()).apply {
                id = R.id.lib_soft_input_layout
                handlerMode = DslSoftInputLayout.MODE_CONTENT_HEIGHT
                addSoftInputListener(this@BaseTitleFragment)
            }
            softInputLayout.addView(view)
            return softInputLayout
        }
        return view
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        onCreateViewAfter(savedInstanceState)
        fragmentUI?.fragmentCreateViewAfter?.invoke(this)
        return view
    }

    /**[onCreateView]*/
    open fun onCreateViewAfter(savedInstanceState: Bundle?) {
        if (enableBackItem()) {
            leftControl()?.append(onCreateBackItem())
        }
    }

    open fun onCreateBackItem(): View? {
        return fragmentUI?.fragmentCreateBackItem?.invoke(this)
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        onInitFragment()
        onInitBehavior()
    }

    /**是否要显示返回按钮*/
    open fun enableBackItem(): Boolean {
        var showBackItem = false
        val count = fragmentManager?.getAllValidityFragment()?.size ?: 0

        if (enableBackItem) {
            /*强制激活了返回按钮*/
            showBackItem = true
        } else if (topFragment() != this) {
            showBackItem = false
        } else if (count <= 0) {
            val activity = activity
            if (activity != null) {

                /*Activity中第一个Fragment*/
                if (!DslAHelper.isMainActivity(activity)) {
                    //当前Fragment所在Activity不是主界面
                    showBackItem = true
                }

                dslIntent {
                    queryAction = Intent.ACTION_MAIN
                    queryCategory = listOf(Intent.CATEGORY_LAUNCHER)
                    queryPackageName = activity.packageName

                    if (doQuery(activity).any { it.activityInfo.name == activity.className() }) {
                        //当前的[Activity]在xml中声明了主页标识
                        showBackItem = false
                    }
                }
            }
        } else {
            /*可见Fragment数量大于0*/
            showBackItem = topFragment() == this
        }

        return showBackItem
    }

    //<editor-fold desc="操作方法">

    fun _inflateTo(wrapId: Int, layoutId: Int) {
        if (layoutId > 0) {
            _vh.visible(wrapId)
            _vh.group(wrapId)?.replace(layoutId)
        } else {
            _vh.gone(wrapId, _vh.group(wrapId)?.childCount ?: 0 <= 0)
        }
    }

    /**初始化样式*/
    open fun onInitFragment() {
        _vh.itemView.isClickable = fragmentConfig.interceptRootTouchEvent

        //内容包裹
        _inflateTo(R.id.lib_content_wrap_layout, contentLayoutId)
        //内容覆盖层
        _inflateTo(R.id.lib_content_overlay_wrap_layout, contentOverlayLayoutId)
        //刷新头包裹
        _inflateTo(R.id.lib_refresh_wrap_layout, refreshLayoutId)
        //标题包裹
        _inflateTo(R.id.lib_title_wrap_layout, titleLayoutId)

        titleControl()?.apply {
            setBackground(fragmentConfig.titleBarBackgroundDrawable)
            selector(R.id.lib_title_text_view)
            setTextSize(fragmentConfig.titleTextSize)
            setTextColor(fragmentConfig.titleTextColor)
        }

        leftControl()?.apply {
            setDrawableColor(fragmentConfig.titleItemIconColor)
            setTextColor(fragmentConfig.titleItemTextColor)
        }

        rightControl()?.apply {
            setDrawableColor(fragmentConfig.titleItemIconColor)
            setTextColor(fragmentConfig.titleItemTextColor)
        }

        rootControl().setBackground(fragmentConfig.fragmentBackgroundDrawable)

        fragmentTitle = fragmentTitle
    }

    /**初始化[Behavior]*/
    open fun onInitBehavior() {
        rootControl().group(R.id.lib_coordinator_wrap_layout)?.eachChild { _, child ->
            onCreateBehavior(child)?.run {
                if (this is RefreshContentBehavior) {
                    refreshContentBehavior = this

                    //刷新监听
                    refreshAction = this@BaseTitleFragment::onRefresh
                }
                child.setBehavior(this)
            }
        }
    }

    /**根据[child]创建对应的[Behavior]*/
    open fun onCreateBehavior(child: View): CoordinatorLayout.Behavior<*>? {
        return child.behavior() ?: when (child.id) {
            //HideTitleBarBehavior(fContext())
            R.id.lib_title_wrap_layout -> TitleBarPlaceholderBehavior(fContext())
            R.id.lib_content_wrap_layout -> RefreshContentBehavior(fContext())
            R.id.lib_refresh_wrap_layout -> if (enableRefresh) {
                ArcLoadingHeaderBehavior(fContext())
            } else {
                _vh.gone(R.id.lib_refresh_wrap_layout)
                RefreshEffectBehavior(fContext())
            }
            else -> null
        }
    }

    /**常用控制助手*/
    open fun titleControl(): DslGroupHelper? =
        _vh.view(R.id.lib_title_wrap_layout)?.run { DslGroupHelper(this) }

    open fun leftControl(): DslGroupHelper? =
        _vh.view(R.id.lib_left_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rightControl(): DslGroupHelper? =
        _vh.view(R.id.lib_right_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rootControl(): DslGroupHelper = DslGroupHelper(_vh.itemView)

    open fun contentControl(): DslGroupHelper? =
        _vh.view(R.id.lib_content_wrap_layout)?.run { DslGroupHelper(this) }

    /**在确保布局已经测量过后, 才执行*/
    fun _laidOut(action: (View) -> Unit) {
        if (ViewCompat.isLaidOut(_vh.itemView)) {
            action(_vh.itemView)
        } else {
            _vh.itemView.doOnPreDraw(action)
        }
    }

    /**开始刷新*/
    open fun startRefresh() {
        _laidOut {
            refreshContentBehavior?.setRefreshContentStatus(IRefreshBehavior.STATUS_REFRESH)
        }
    }

    /**结束刷新*/
    open fun finishRefresh() {
        _laidOut {
            refreshContentBehavior?.setRefreshContentStatus(IRefreshBehavior.STATUS_FINISH)
        }
    }

    /**刷新回调*/
    open fun onRefresh(refreshContentBehavior: IRefreshContentBehavior?) {

    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="扩展方法">

    fun DslGroupHelper.appendItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        appendTextItem {
            gravity = Gravity.CENTER
            setTextColor(fragmentConfig.titleItemTextColor)
            this.text = span {

                if (ico != undefined_res) {
                    drawable {
                        backgroundDrawable =
                            loadDrawable(ico).colorFilter(fragmentConfig.titleItemIconColor)
                        textGravity = Gravity.CENTER
                    }
                }

                if (text != null) {
                    drawable(text) {
                        textGravity = Gravity.CENTER
                    }
                }
            }
            clickIt(onClick)
            this.action()
        }
    }

    fun appendLeftItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        leftControl()?.appendItem(text, ico, action, onClick)
    }

    fun appendRightItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        rightControl()?.appendItem(text, ico, action, onClick)
    }

    //</editor-fold desc="扩展方法">

    //<editor-fold desc="软键盘监听">

    override fun onSoftInputChangeStart(action: Int, height: Int, oldHeight: Int) {
        super.onSoftInputChangeStart(action, height, oldHeight)
        if (action.isHideAction()) {
            //是隐藏动作
        }
    }

    override fun onSoftInputChangeEnd(action: Int, height: Int, oldHeight: Int) {
        super.onSoftInputChangeEnd(action, height, oldHeight)
        if (action.isShowAction()) {
            //是显示动作
        }
    }

    override fun onSoftInputChange(action: Int, height: Int, oldHeight: Int, fraction: Float) {
        super.onSoftInputChange(action, height, oldHeight, fraction)
    }

    //</editor-fold desc="软键盘监听">

    //<editor-fold desc="情感图切换">

    fun installAffect(viewGroup: ViewGroup?) {
        affectUI = dslAffect(viewGroup) {
            affectChangeBefore = this@BaseTitleFragment::onAffectChangeBefore
            affectChanged = this@BaseTitleFragment::onAffectChanged
        }
        viewGroup?.visible()
    }

    fun onAffectChangeBefore(dslAffect: DslAffect, from: Int, to: Int, data: Any?): Boolean {
        return false
    }

    fun onAffectChanged(
        dslAffect: DslAffect,
        from: Int,
        to: Int,
        fromView: View?,
        toView: View,
        data: Any?
    ) {
        if (to == DslAffect.AFFECT_LOADING) {
            //触发刷新
            onRefresh(null)
        }
    }

    /**显示加载中*/
    fun affectLoading() {
        affectUI ?: installAffect(_vh.v(R.id.lib_content_overlay_wrap_layout))
        affectUI?.showAffect(DslAffect.AFFECT_LOADING)
    }

    /**显示内容*/
    fun affectContent() {
        affectUI ?: installAffect(_vh.v(R.id.lib_content_overlay_wrap_layout))
        affectUI?.showAffect(DslAffect.AFFECT_CONTENT)
    }

    // </editor-fold desc="情感图切换">
}

/**设置为一个简单的内嵌列表界面*/
fun BaseTitleFragment.singleRecycler() {
    fragmentLayoutId = R.layout.lib_recycler_layout
}

/**设置为一个简单的内嵌列表界面, 请在[onCreateView]之后使用*/
fun BaseTitleFragment.hideTitle() {
    //titleLayoutId = R.layout.lib_empty_item
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        _vh.gone(R.id.lib_title_wrap_layout)
    } else {
        lifecycle.onStart {
            _vh.gone(R.id.lib_title_wrap_layout)
            true
        }
    }
}