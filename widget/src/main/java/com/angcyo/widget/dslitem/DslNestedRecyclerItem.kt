package com.angcyo.widget.dslitem

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.recycler.*

/**
 * 内嵌[RecyclerView]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslNestedRecyclerItem : DslAdapterItem() {

    /**内嵌适配器*/
    var itemNestedAdapter: DslAdapter = DslAdapter()

    /**布局管理,
     * 请注意使用:recycleChildrenOnDetach*/
    var itemNestedLayoutManager: RecyclerView.LayoutManager? = null

    /**自动恢复滚动位置*/
    var itemKeepScrollPosition = true

    init {
        itemLayoutId = R.layout.dsl_nested_recycler_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        onBindRecyclerView(itemHolder, itemPosition, adapterItem, payloads)
    }

    var _onScrollListener: RecyclerView.OnScrollListener? = null

    var _scrollPositionConfig: ScrollPositionConfig? = null

    open fun onBindRecyclerView(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //列表
        itemHolder.rv(R.id.lib_nested_recycler_view)?.apply {
            //优先清空[OnScrollListener]
            clearOnScrollListeners()
            clearItemDecoration()
            initDsl()
            layoutManager = itemNestedLayoutManager
            adapter = itemNestedAdapter

            if (itemKeepScrollPosition) {
                _scrollPositionConfig?.run { restoreScrollPosition(this) }
            }

            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    _scrollPositionConfig = saveScrollPosition()
                }
            }.apply {
                _onScrollListener = this
                addOnScrollListener(this)
            }
        }
    }
}