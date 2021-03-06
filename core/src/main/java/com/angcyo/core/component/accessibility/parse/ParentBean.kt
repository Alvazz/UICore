package com.angcyo.core.component.accessibility.parse

/**
 * 递归parent查询满足条件的节点
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/10/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class ParentBean(
    /**
     * [+1] parent往下查找
     * [-1] parent往上查找
     * */
    var path: String? = "+1",

    /**查询的深度, 超过后直接返回, 不管有没有找到*/
    var depth: Int = 5,

    /**满足[ConstraintBean]条件的节点*/
    var checkList: List<ConstraintBean>? = null
)