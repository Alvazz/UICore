package com.angcyo.loader

import com.angcyo.library.L
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */
open class FolderCreator {
    fun creatorFolder(config: LoaderConfig, allMedia: List<LoaderMedia>): List<LoaderFolder> {
        val result = mutableListOf<LoaderFolder>()
        var allImage: LoaderFolder? = null
        var allVideo: LoaderFolder? = null
        var allAudio: LoaderFolder? = null
        var allImageAndVideo: LoaderFolder? = null

        val mediaLoaderType = config.mediaLoaderType
        if (mediaLoaderType and Config.LOADER_TYPE_IMAGE == Config.LOADER_TYPE_IMAGE) {
            if (mediaLoaderType and Config.LOADER_TYPE_VIDEO == Config.LOADER_TYPE_VIDEO) {
                allImageAndVideo = LoaderFolder("图片和视频", "allImageAndVideo")
                result.add(allImageAndVideo)
            }

            allImage = LoaderFolder("所有图片", "allImage")
            result.add(allImage)
        }

        if (mediaLoaderType and Config.LOADER_TYPE_VIDEO == Config.LOADER_TYPE_VIDEO) {
            allVideo = LoaderFolder("所有视频", "allVideo")
            result.add(allVideo)
        }

        if (mediaLoaderType and Config.LOADER_TYPE_AUDIO == Config.LOADER_TYPE_AUDIO) {
            allAudio = LoaderFolder("所有音频", "allAudio")
            result.add(allAudio)
        }

        loop@ for (media in allMedia) {
            try {
                val file = File(media.localPath!!)

                if (!file.canRead()) {
                    continue
                }

                when {
                    media.isImage() -> {
                        allImage?.mediaItemList?.add(media)
                        allImageAndVideo?.mediaItemList?.add(media)
                    }
                    media.isVideo() -> {
                        if (media.duration <= 0) {
                            continue@loop
                        }

                        allImageAndVideo?.mediaItemList?.add(media)
                        allVideo?.mediaItemList?.add(media)
                    }
                    media.isAudio() -> {
                        if (media.duration <= 0) {
                            continue@loop
                        }

                        allAudio?.mediaItemList?.add(media)
                    }
                }

                val folderPath = file.parentFile?.absolutePath
                val folderName = file.parentFile?.name

                val folder =
                    result.find { it.folderName == folderName } ?: LoaderFolder(
                        folderName,
                        folderPath
                    ).apply {
                        result.add(this)
                    }
                folder.mediaItemList.add(media)
            } catch (e: Exception) {
                L.w(e)
            }
        }

        return result
    }
}