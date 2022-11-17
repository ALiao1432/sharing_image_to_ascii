package com.redso.sharing_image_to_ascii.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.redso.sharing_image_to_ascii.extension.BitmapRotateDegree
import com.redso.sharing_image_to_ascii.extension.rotate
import com.redso.sharing_image_to_ascii.extension.scale

class MainViewModel : ViewModel() {
    private val asciiText = "            .,_-~'=+^:;cba!?IO0123456789B$&WM#@Ñ☯🀫◉✿☻︎"
    var handleShotButtonViewClicked: (() -> Unit)? = null

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        buffer.clear()
        return bitmap
    }

    fun getProcessBitmap(image: ImageProxy) = imageProxyToBitmap(image)
        .rotate(BitmapRotateDegree.Degree_90)
        .scale(Size(60, 80))

    fun getLuminanceArray(bitmap: Bitmap): Array<IntArray> {
        val luminanceArray = Array(bitmap.height) { IntArray(bitmap.width) }
        for (j in 0 until bitmap.height) {
            for (i in 0 until bitmap.width) {
                luminanceArray[j][i] = (bitmap.getColor(i, j).luminance() * 100).toInt()
            }
        }
        return luminanceArray
    }

    fun getAsciiTextByLuminance(luminance: Int): String {
        var index = asciiText.length * luminance / 100
        if (index == asciiText.length) {
            index -= 1
        }
        return asciiText[index].toString()
    }
}