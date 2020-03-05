package com.angcyo.core.component

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.HorizontalScrollView
import com.angcyo.DslFHelper
import com.angcyo.base.dslFHelper
import com.angcyo.core.R
import com.angcyo.core.component.file.DslFileLoader
import com.angcyo.core.component.file.FileItem
import com.angcyo.core.component.file.file
import com.angcyo.core.dslitem.DslFileSelectorItem
import com.angcyo.core.fragment.BaseFragment
import com.angcyo.dsladapter.*
import com.angcyo.library.ex.isFile
import com.angcyo.library.ex.isFileScheme
import com.angcyo.library.ex.isFolder
import com.angcyo.library.ex.withMinValue
import com.angcyo.library.toastQQ
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.doAnimate
import com.angcyo.widget.base.drawWidth
import com.angcyo.widget.layout.touch.TouchBackLayout
import com.angcyo.widget.progress.HSProgressView
import com.angcyo.widget.recycler.initDslAdapter
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/29
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class FileSelectorFragment : BaseFragment() {

    private var fileSelectorConfig = FileSelectorConfig()

    /**获取上一层路径*/
    private fun getPrePath(): String =
        fileSelectorConfig.targetPath.substring(0, fileSelectorConfig.targetPath.lastIndexOf("/"))

    private var scrollView: HorizontalScrollView? = null

    /**选中的文件item*/
    private var selectorFileItem: FileItem? = null

    init {
        fragmentLayoutId = R.layout.lib_file_selector_fragment
    }

    override fun canSwipeBack(): Boolean {
        return false
    }

    override fun onBackPressed(): Boolean {
        doHideAnimator {
            removeFragment()
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //默认选中
        fileSelectorConfig.selectorFileUri?.run {
            selectorFileItem = FileItem(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doShowAnimator()
    }

    /**
     * 调用此方法, 配置参数
     * */
    fun fileSelectorConfig(config: FileSelectorConfig.() -> Unit): FileSelectorFragment {
        this.fileSelectorConfig.config()
        return this
    }

    lateinit var _adapter: DslAdapter

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        //半屏效果
        _vh.v<TouchBackLayout>(R.id.lib_touch_back_layout)?.apply {
            enableTouchBack = true
            offsetScrollTop = (resources.displayMetrics.heightPixels) / 2

            onTouchBackListener = object : TouchBackLayout.OnTouchBackListener {
                override fun onTouchBackListener(
                    layout: TouchBackLayout,
                    oldScrollY: Int,
                    scrollY: Int,
                    maxScrollY: Int
                ) {
                    if (scrollY >= maxScrollY) {
                        removeFragment()
                    }
                }
            }
        }

        _vh.tv(R.id.current_file_path_view)?.text = fileSelectorConfig.targetPath
        _vh.view(R.id.file_selector_button)?.isEnabled = false

        scrollView = _vh.v(R.id.current_file_path_layout)

        /*上一个路径*/
        _vh.click(R.id.current_file_path_layout) {
            resetPath(getPrePath())
        }
        //选择按钮
        _vh.click(R.id.file_selector_button) {
            //T_.show(selectorFilePath)
            doHideAnimator {
                removeFragment()
                fileSelectorConfig.onFileSelector?.invoke(selectorFileItem)
                fileSelectorConfig = FileSelectorConfig()
            }
        }

        _vh.rv(R.id.lib_recycler_view)?.apply {
            _adapter = initDslAdapter()
            _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)
            _adapter.singleModel()

            _adapter.selector().observer {
                onItemChange = { selectorItems, selectorIndexList, _, _ ->
                    selectorFileItem =
                        (selectorItems.firstOrNull() as? DslFileSelectorItem)?.itemFile
                    _vh.enable(R.id.file_selector_button, selectorIndexList.isNotEmpty())
                }
            }
        }

        //文件列表加载返回
        dslFileLoader.onLoaderResult = {
            _vh.gone(R.id.lib_progress_view)
            _adapter.loadSingleData2<DslFileSelectorItem>(it, 1, Int.MAX_VALUE) { data ->
                itemShowFileMd5 = fileSelectorConfig.showFileMd5
                itemFile = data as? FileItem
                itemIsSelected = selectorFileItem == itemFile

                onItemClick = {
                    itemFile?.apply {
                        if (file().isFile()) {
                            _adapter.select {
                                it == this@loadSingleData2
                            }
                        } else if (file().isFolder()) {
                            resetPath(file()!!.absolutePath)
                        }
                    }
                }

                if (fileSelectorConfig.showFileMenu) {
                    onItemLongClick = {
                        toastQQ("菜单")
                        true
                    }
                }
            }
        }

        //文件耗时操作返回
        dslFileLoader.onLoaderDelayResult = { fileItem ->
            _adapter.updateItem {
                (it as? DslFileSelectorItem)?.itemFile == fileItem
            }
        }
    }

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        loadPath(fileSelectorConfig.targetPath, 360)
    }

    private fun setSelectorFilePath(uri: Uri) {
        fileSelectorConfig.selectorFileUri = uri
        _vh.view(R.id.file_selector_button)?.isEnabled = uri.isFileScheme()
    }

    private fun resetPath(path: String, force: Boolean = false) {
        //L.e("call: resetPath -> $path")
        fileSelectorConfig.targetPath = path
        if (!force && _vh.tv(R.id.current_file_path_view)?.text.toString() == fileSelectorConfig.targetPath) {
            return
        }
        loadPath(path)
    }

    private fun loadPath(path: String, delay: Long = 0L) {
        fileSelectorConfig.targetPath = path
        //_vh.view(R.id.base_selector_button).isEnabled = false
        _vh.tv(R.id.current_file_path_view)?.text = fileSelectorConfig.targetPath

        scrollView?.let {
            it.post {
                val x = it.getChildAt(0).measuredWidth - it.drawWidth
                it.scrollTo(x.withMinValue(0), 0)
            }
        }

        loadFileList(fileSelectorConfig.targetPath, delay)
    }

    //文件loader
    val dslFileLoader = DslFileLoader()

    private fun loadFileList(path: String, delay: Long = 0L) {
        _vh.v<HSProgressView>(R.id.lib_progress_view)?.apply {
            visibility = View.VISIBLE
            startAnimator()
        }
        _vh.postDelay(delay) {
            dslFileLoader.loadHideFile = fileSelectorConfig.showHideFile
            dslFileLoader.load(path)
        }
    }

    private fun doShowAnimator() {
        _vh.view(R.id.lib_touch_back_layout)?.run {
            doAnimate {
                translationY = this.measuredHeight.toFloat()
                animate().translationY(0f).setDuration(Anim.ANIM_DURATION).start()
            }
        }
    }

    private fun doHideAnimator(onEnd: () -> Unit) {
        _vh.view(R.id.lib_touch_back_layout)?.run {
            animate()
                .translationY(this.measuredHeight.toFloat())
                .setDuration(Anim.ANIM_DURATION)
                .withEndAction(onEnd)
                .start()
        }
    }

    private fun removeFragment() {
        dslFHelper {
            noAnim()
            remove(this@FileSelectorFragment)
        }
    }
}

open class FileSelectorConfig {

    /**是否显示隐藏文件*/
    var showHideFile = false

    /**是否显示文件MD5值*/
    var showFileMd5 = false

    /**是否长按显示文件菜单*/
    var showFileMenu = false

    /**最根的目录*/
    var storageDirectory = Environment.getExternalStorageDirectory().absolutePath
        set(value) {
            if (File(value).exists()) {
                field = value
                targetPath = value
            }
        }

    /**目标路径*/
    var targetPath: String = storageDirectory
        set(value) {
            if (value.isNotEmpty() && value.startsWith(storageDirectory)) {
                val file = File(value)
                if (file.isDirectory) {
                    field = value
                } else if (file.isFile) {
                    field = file.parent
                }
            } else {
                field = storageDirectory
            }
        }

    /*默认选中的文件*/
    var selectorFileUri: Uri? = null

    var onFileSelector: ((FileItem?) -> Unit)? = null
}

/**文件选择*/
fun DslFHelper.fileSelector(
    config: FileSelectorConfig.() -> Unit = {},
    onResult: (FileItem?) -> Unit = {}
) {
    noAnim()
    show(FileSelectorFragment().apply {
        fileSelectorConfig {
            config()
            onFileSelector = onResult
        }
    })
}