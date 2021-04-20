package com.example.arthanfinance.applyLoan

import android.R.attr
import android.graphics.Bitmap
import android.util.Base64
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


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

            val fOut = FileOutputStream(fileName)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()

            val fos = FileOutputStream(fileName)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            subscriber.onNext(fileName.absolutePath)
        }
    }
}