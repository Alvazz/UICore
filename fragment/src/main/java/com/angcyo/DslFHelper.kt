package com.angcyo

import android.app.Activity
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.angcyo.base.*
import com.angcyo.fragment.IFragment
import com.angcyo.fragment.R
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */
class DslFHelper(val fm: FragmentManager, val debug: Boolean = false) {

    /**这个列表中的[Fragment]将会被执行[add]操作*/
    val showFragmentList = mutableListOf<Fragment>()

    /**这个列表中的[Fragment]将会被执行[remove]操作*/
    val removeFragmentList = mutableListOf<Fragment>()

    /**[Fragment]默认的容器id*/
    @IdRes
    var containerViewId: Int = fm.getLastFragmentContainerId(R.id.fragment_container)

    /**
     * 隐藏[hide], 操作之后最后一个[Fragment]前面第几个开始的所有[Fragment]
     * >=1 才有效.
     * */
    var hideBeforeIndex = 1

    /**最后一个[Fragment]在执行的[back]时, 是否需要[remove]*/
    var removeLastFragment: Boolean = false

    init {
        //FragmentManager.enableDebugLogging(debug)
        if (debug) {
            fm.registerFragmentLifecycleCallbacks(LogFragmentLifecycleCallbacks(), true)
        }
    }

    //<editor-fold desc="add 或者 show操作">

    fun show(vararg fClass: Class<out Fragment>) {
        val list = mutableListOf<Fragment>()
        for (cls in fClass) {
            list.add(
                fm.fragmentFactory.instantiate(
                    cls.classLoader!!,
                    cls.name
                )
            )
        }
        show(list)
    }

    fun show(vararg fragment: Fragment) {
        show(fragment.toList())
    }

    /**
     * 如果显示的[Fragment]已经[add], 那么此[Fragment]上面的其他[Fragment]都将被[remove]
     * 这一点, 有点类似[Activity]的[SINGLE_TASK]启动模式
     * */
    fun show(fragmentList: List<Fragment>) {
        fragmentList.forEach {
            if (!showFragmentList.contains(it)) {
                showFragmentList.add(it)
                if (it.isAdded) {
                    remove(fm.getOverlayFragment(it))
                }
            }
        }
    }

    fun restore(vararg tag: String?) {
        show(fm.restore(*tag))
    }

    fun restore(vararg fragment: Fragment) {
        show(fm.restore(*fragment))
    }

    //</editor-fold desc="add 或者 show操作">

    //<editor-fold desc="remove操作">

    fun remove(vararg fClass: Class<out Fragment>) {
        for (cls in fClass) {
            remove(cls.name)
        }
    }

    fun remove(vararg tag: String?) {
        for (t in tag) {
            fm.findFragmentByTag(t)?.let { remove(it) }
        }
    }

    fun remove(fragment: Fragment?) {
        if (fragment == null) {
            return
        }
        if (!removeFragmentList.contains(fragment)) {
            removeFragmentList.add(fragment)
        }
    }

    fun removes(vararg fragment: Fragment) {
        remove(fragment.toList())
    }

    fun remove(fragmentList: List<Fragment>) {
        for (f in fragmentList) {
            remove(f)
        }
    }

    fun removeLast() {
        remove(fm.getAllValidityFragment().lastOrNull())
    }

    /**移除所有[getView]不为空的[Fragment]*/
    fun removeAll() {
        remove(fm.getAllValidityFragment())
    }

    //</editor-fold desc="remove操作">

    /**自定义的配置操作, 请勿在此执行[commit]操作*/
    var onConfigTransaction: (FragmentTransaction) -> Unit = {

    }

    /**提交操作*/
    var onCommit: (FragmentTransaction) -> Unit = {
        //        if (debug) {
//            it.runOnCommit {
//                fm.log()
//            }
//        }

        if (fm.isStateSaved) {
            it.commitNowAllowingStateLoss()
        } else {
            it.commitNow()
        }
    }

    /**
     * 回退操作, 请使用此方法.
     * 会进行回退检查
     *
     * @return 返回true, 表示当前Activity可以被关闭
     * */
    fun back(): Boolean {
        var result = true

        val allValidityFragment = fm.getAllValidityFragment()
        val lastFragment = allValidityFragment.lastOrNull()

        if (lastFragment != null) {
            if (lastFragment is IFragment) {
                if (!lastFragment.onBackPressed()) {
                    result = false
                }
            }

            if (result) {
                if (showFragmentList.isEmpty() && allValidityFragment.size == 1) {
                    if (removeLastFragment) {
                        //只有一个Fragment
                        remove(lastFragment)
                        doIt()
                    }
                } else {
                    remove(lastFragment)
                    doIt()
                }
            }
        }

        return result
    }

    /**执行操作*/
    fun doIt() {
        fm.beginTransaction().apply {
            if (fm.isDestroyed) {
                //no op
                L.w("fm is destroyed.")
                return
            }

            if (removeFragmentList.isEmpty() && showFragmentList.isEmpty()) {
                L.w("no op do it.")
                return
            }

            //一顿操作之后, 最终fm中, 应该有的Fragment列表
            val fmFragmentList = mutableListOf<Fragment>()

            fmFragmentList.addAll(fm.fragments)
            fmFragmentList.addAll(showFragmentList)
            fmFragmentList.removeAll(removeFragmentList)

            //...end

            val lastFragment = fmFragmentList.lastOrNull()

            //op remove
            removeFragmentList.forEach {
                remove(it)
            }

            //op show
            showFragmentList.forEach { fragment ->
                when {
                    fragment.isDetached -> attach(fragment)
                    fragment.isAdded -> {
                    }
                    else -> add(
                        containerViewId,
                        fragment,
                        fragment.getFragmentTag()
                    )
                }
                if (fragment != lastFragment) {
                    setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }
            }

            //op hide
            if (hideBeforeIndex >= 1) {
                fmFragmentList.forEachIndexed { index, fragment ->
                    if (index < fmFragmentList.size - hideBeforeIndex) {
                        setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                        hide(fragment)
                    } else {
                        show(fragment)
                    }
                }
            }

            //op last
            lastFragment?.let { setMaxLifecycle(it, Lifecycle.State.RESUMED) }

            onConfigTransaction(this)

            onCommit(this)
        }
    }
}