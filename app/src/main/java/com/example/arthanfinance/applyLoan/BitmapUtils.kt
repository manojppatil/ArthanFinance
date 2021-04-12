package com.example.arthanfinance.applyLoan

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import io.reactivex.Observable

object BitmapUtils {

    fun getBase64(bm: Bitmap): String {
        return try {
            val outputStream = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap, fileName: File): Observable<String> {
        return io.reactivex.Observable.create { subscriber ->

            /*val pictureFile = if (fileName == null)
                FileUtil.createImgFile()
            else
                FileUtil.createImgFile(fileName)*/

            /*val matrix = Matrix()
            //this will prevent mirror effect
            if (fromFrontCam)
                matrix.preScale(-1.0f, 1.0f)
            val b = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)*/

            val fos = FileOutputStream(fileName)
            val isCompresssed = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            subscriber.onNext(fileName.absolutePath)
        }
    }
}