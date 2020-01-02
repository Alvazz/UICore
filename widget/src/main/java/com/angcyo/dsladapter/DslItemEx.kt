package com.angcyo.dsladapter

import android.graphics.Color
import androidx.annotation.LayoutRes
import com.angcyo.drawable.dpi
import com.angcyo.widget.R


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**
 * 通过条件, 查找[DslAdapterItem].
 *
 * @param useFilterList 是否使用过滤后的数据源. 通常界面上显示的是过滤后的数据, 所有add的数据源在非过滤列表中
 * */
public fun DslAdapter.findItem(
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return getDataList(true).find(predicate)
}

public fun DslAdapter.findItemByTag(
    tag: String,
    useFilterList: Boolean = true
): DslAdapterItem? {
    return findItem(useFilterList) {
        it.itemTag == tag
    }
}

public fun DslAdapter.dslItem(@LayoutRes layoutId: Int, config: DslAdapterItem.() -> Unit = {}) {
    val item = DslAdapterItem()
    item.itemLayoutId = layoutId
    addLastItem(item)
    item.config()
}

public fun <T : DslAdapterItem> DslAdapter.dslItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    dslCustomItem(dslItem, config)
}

public fun <T : DslAdapterItem> DslAdapter.dslCustomItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    addLastItem(dslItem)
    dslItem.config()
}

/**空的占位item*/
public fun DslAdapter.renderEmptyItem(height: Int = 120 * dpi, color: Int = Color.TRANSPARENT) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemLayoutId = R.layout.lib_empty_item
    adapterItem.onItemBindOverride = { itemHolder, _, _ ->
        itemHolder.itemView.setBackgroundColor(color)
        itemHolder.itemView.setHeight(height)
    }
    addLastItem(adapterItem)
}

public fun DslAdapter.renderItem(count: Int = 1, init: DslAdapterItem.(index: Int) -> Unit) {
    for (i in 0 until count) {
        val adapterItem = DslAdapterItem()
        adapterItem.init(i)
        addLastItem(adapterItem)
    }
}

public fun <T> DslAdapter.renderItem(data: T, init: DslAdapterItem.() -> Unit) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemData = data
    adapterItem.init()
    addLastItem(adapterItem)
}