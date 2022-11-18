package com.redso.sharing_image_to_ascii.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.redso.sharing_image_to_ascii.extension.BitmapRotateDegree
import com.redso.sharing_image_to_ascii.extension.rotate
import com.redso.sharing_image_to_ascii.extension.scale
import com.redso.sharing_image_to_ascii.util.MyApp
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {
    private val asciiText = "            .,_˙-~ˋ'ˇ=+^:;cba!?([{IO0123456789B$&WM#@Ñ"
    var handleShotButtonViewClicked: (() -> Unit)? = null
    var handleSharingButtonImageClicked: (() -> Unit)? = null

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        buffer.clear()
        return bitmap
    }

    fun getProcessBitmap(image: ImageProxy, scaleSize: Size) = imageProxyToBitmap(image)
        .rotate(BitmapRotateDegree.Degree_90)
        .scale(scaleSize)

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

    fun convertBitmapToFile(bitmap: Bitmap): File {
        val FILENAME_DATETIME_FORMAT = "yyyy-MM-dd-EEE-HH_mm_ss"
        val FILENAME_APPEND_NAME = "ascii.jpg"

        val fileName = "${
            SimpleDateFormat(
                FILENAME_DATETIME_FORMAT,
                Locale.ENGLISH
            ).format(System.currentTimeMillis())
        }_$FILENAME_APPEND_NAME"

        //create a file to write bitmap data
        val file = File(MyApp.me.cacheDir, fileName)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            bos.close()
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}
