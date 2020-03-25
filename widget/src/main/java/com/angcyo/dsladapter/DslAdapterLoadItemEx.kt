package com.angcyo.dsladapter

import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**用于[Adapter]中单一数据类型的列表*/
inline fun <reified Item : DslAdapterItem> DslAdapter.loadSingleData(
    dataList: List<Any>?,
    page: Int = 1,
    pageSize: Int = DslAdapter.DEFAULT_PAGE_SIZE,
    filterParams: FilterParams = defaultFilterParams!!.apply {
        payload = listOf(
            DslAdapterItem.PAYLOAD_UPDATE_PART,
            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
        )
    },
    crossinline initOrCreateDslItem: (oldItem: Item?, data: Any) -> Item
) {
    changeDataItems(filterParams) {
        //移除所有不同类型的item
        val removeItemList = mutableListOf<DslAdapterItem>()
        it.forEach { item ->
            if (item !is Item) {
                removeItemList.add(item)
            }
        }
        it.removeAll(removeItemList)

        //加载数据
        val list = dataList ?: emptyList()
        //第一页 数据检查
        if (page <= 1) {
            if (it.size > list.size) {
                for (i in max(it.lastIndex, 0) downTo max(list.size, 0)) {
                    it.removeAt(i)
                }
            }
            //重新旧数据
            it.forEachIndexed { index, dslAdapterItem ->
                val data = list[index]
                dslAdapterItem.itemChanging = dslAdapterItem.itemData != data
                dslAdapterItem.itemData = data
                initOrCreateDslItem(dslAdapterItem as Item, data)
            }
            if (list.size > it.size) {
                //需要补充新的DslAdapterItem
                for (i in it.size until list.size) {
                    val data = list[i]
                    val dslItem = initOrCreateDslItem(null, data)
                    dslItem.itemData = data
                    it.add(dslItem)
                }
            }
            if (it.isEmpty() && headerItems.isEmpty() && footerItems.isEmpty()) {
                //空数据
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
            } else {
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
                if (dslLoadMoreItem.itemStateEnable) {
                    if (it.size < pageSize) {
                        setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NO_MORE)
                    } else {
                        setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NORMAL)
                    }
                }
            }
        } else {
            //第二页 追加数据检查
            for (data in list) {
                val dslItem = initOrCreateDslItem(null, data)
                dslItem.itemData = data
                it.add(dslItem)
            }
            setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
            if (dslLoadMoreItem.itemStateEnable) {
                if (list.size < pageSize) {
                    setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NO_MORE)
                } else {
                    setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NORMAL)
                }
            }
        }
    }
}

inline fun <reified Item : DslAdapterItem> DslAdapter.loadSingleData2(
    dataList: List<Any>?,
    page: Int = 1,
    pageSize: Int = DslAdapter.DEFAULT_PAGE_SIZE,
    filterParams: FilterParams = defaultFilterParams!!.apply {
        payload = listOf(
            DslAdapterItem.PAYLOAD_UPDATE_PART,
            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
        )
    },
    crossinline initItem: Item.(data: Any) -> Unit = {}
) {
    loadSingleData<Item>(dataList, page, pageSize, filterParams) { oldItem, data ->
        (oldItem ?: Item::class.java.newInstance()).apply {
            initItem(data)
        }
    }
}

class UpdateDataConfig {
    /**需要加载的页码, 会偏移到指定位置*/
    var updatePage: Int = 1

    /**页面数量*/
    var pageSize: Int = DslAdapter.DEFAULT_PAGE_SIZE

    /**需要加载的数据*/
    var updateDataList: List<Any>? = null

    var filterParams: FilterParams = FilterParams().apply {
        payload = listOf(
            DslAdapterItem.PAYLOAD_UPDATE_PART,
            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
        )
    }

    /**
     * 更新已有的item, 创建不存在的item, 移除不需要的item
     * [oldItem] 如果有值, 则希望更新[oldItem]
     * @return 返回null, 则会删除对应的[oldItem]
     * */
    var updateOrCreateItem: (oldItem: DslAdapterItem?, data: Any?, index: Int) -> DslAdapterItem? =
        { oldItem, data, index ->
            oldItem
        }
}

fun DslAdapter.updateData(action: UpdateDataConfig.() -> Unit) {
    val config = UpdateDataConfig()
    config.action()

//    changeDataItems(config.filterParams) {
//        //加载数据
//        val list = config.updateDataList ?: emptyList()
//        //第一页 数据检查
//        if (config.updatePage <= 1) {
//            if (it.size > list.size) {
//                for (i in max(it.lastIndex, 0) downTo max(list.size, 0)) {
//                    it.removeAt(i)
//                }
//            }
//            //重新旧数据
//            it.forEachIndexed { index, dslAdapterItem ->
//                val data = list[index]
//                dslAdapterItem.itemChanging = dslAdapterItem.itemData != data
//                dslAdapterItem.itemData = data
//                initOrCreateDslItem(dslAdapterItem as Item, data)
//            }
//            if (list.size > it.size) {
//                //需要补充新的DslAdapterItem
//                for (i in it.size until list.size) {
//                    val data = list[i]
//                    val dslItem = initOrCreateDslItem(null, data)
//                    dslItem.itemData = data
//                    it.add(dslItem)
//                }
//            }
//            if (it.isEmpty() && headerItems.isEmpty() && footerItems.isEmpty()) {
//                //空数据
//                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
//            } else {
//                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
//                if (dslLoadMoreItem.itemStateEnable) {
//                    if (it.size < pageSize) {
//                        setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NO_MORE)
//                    } else {
//                        setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NORMAL)
//                    }
//                }
//            }
//        } else {
//            //第二页 追加数据检查
//            for (data in list) {
//                val dslItem = initOrCreateDslItem(null, data)
//                dslItem.itemData = data
//                it.add(dslItem)
//            }
//            setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
//            if (dslLoadMoreItem.itemStateEnable) {
//                if (list.size < pageSize) {
//                    setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NO_MORE)
//                } else {
//                    setLoadMore(DslLoadMoreItem.ADAPTER_LOAD_NORMAL)
//                }
//            }
//        }
//    }
}