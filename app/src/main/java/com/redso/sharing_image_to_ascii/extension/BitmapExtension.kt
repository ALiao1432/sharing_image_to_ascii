package com.redso.sharing_image_to_ascii.extension

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size

enum class BitmapRotateDegree(val value: Float) {
    Degree_90(90F),
    Degree_180(180F),
    Degree_270(270F),
}

fun Bitmap.rotate(degree: BitmapRotateDegree): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.value)

//    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.scale(size: Size): Bitmap {
    return Bitmap.createScaledBitmap(this, size.width, size.height, false)
}