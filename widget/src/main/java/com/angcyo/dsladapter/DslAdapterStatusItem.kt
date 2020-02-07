package com.angcyo.dsladapter

import com.angcyo.library.L
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.base.setWidthHeight

/**
 * [DslAdapter] 中, 控制情感图显示状态的 [Item]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslAdapterStatusItem : BaseDslStateItem() {

    companion object {
        /**正常状态, 切换到内容*/
        const val ADAPTER_STATUS_NONE = 0
        /**空数据*/
        const val ADAPTER_STATUS_EMPTY = 1
        /**加载中*/
        const val ADAPTER_STATUS_LOADING = 2
        /**错误*/
        const val ADAPTER_STATUS_ERROR = 3
    }

    /**刷新回调*/
    var onRefresh: (DslViewHolder) -> Unit = {
        L.i("[DslAdapterStatusItem] 触发刷新")
    }

    //是否已经在刷新, 防止重复触发回调
    var _isRefresh = false

    init {
        itemStateLayoutMap[ADAPTER_STATUS_LOADING] = R.layout.lib_loading_layout
        itemStateLayoutMap[ADAPTER_STATUS_ERROR] = R.layout.lib_error_layout
        itemStateLayoutMap[ADAPTER_STATUS_EMPTY] = R.layout.lib_empty_layout

        itemState = ADAPTER_STATUS_NONE
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        itemHolder.itemView.setWidthHeight(-1, -1)
        super.onItemBind(itemHolder, itemPosition, adapterItem)
    }

    override fun _onBindStateLayout(itemHolder: DslViewHolder, state: Int) {
        super._onBindStateLayout(itemHolder, state)
        if (itemState == ADAPTER_STATUS_ERROR) {
            //出现错误后, 触击刷新
            itemHolder.clickItem {
                if (itemState == ADAPTER_STATUS_ERROR) {
                    _notifyRefresh(itemHolder)
                    itemDslAdapter?.setAdapterStatus(ADAPTER_STATUS_LOADING)
                }
            }
            itemHolder.click(R.id.lib_retry_button) {
                itemHolder.clickView(itemHolder.itemView)
            }
        } else if (itemState == ADAPTER_STATUS_LOADING) {
            _notifyRefresh(itemHolder)
        } else {
            itemHolder.itemView.isClickable = false
        }
    }

    open fun _notifyRefresh(itemHolder: DslViewHolder) {
        if (!_isRefresh) {
            _isRefresh = true
            itemHolder.post { onRefresh(itemHolder) }
        }
    }

    override fun _onItemStateChange(old: Int, value: Int) {
        if (old != value && value != ADAPTER_STATUS_LOADING) {
            _isRefresh = false
        }
        super._onItemStateChange(old, value)
    }
}