package com.shersoft.test.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.shersoft.test.BuildConfig
import com.shersoft.test.MainActivity
import java.io.*


class ShareFile(var contexts: MainActivity) {
    @Throws(IOException::class)
    fun sendAppItself(paramActivity: Activity) {
        val pm = paramActivity.packageManager
        val appInfo: ApplicationInfo
        try {
            appInfo = pm.getApplicationInfo(
                paramActivity.packageName,
                PackageManager.GET_META_DATA
            )
            val sendBt = Intent(Intent.ACTION_SEND)
            sendBt.type = "*/*"
            sendBt.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse("file://" + appInfo.publicSourceDir)
            )
            paramActivity.startActivity(
                Intent.createChooser(
                    sendBt,
                    "Share it using"
                )
            )
        } catch (e1: PackageManager.NameNotFoundException) {
            e1.printStackTrace()
        }
    }

    fun shareAppAsAPK(context: Context) {
        val app: ApplicationInfo = context.applicationInfo
        val originalApk = app.publicSourceDir
        try {
            //Make new directory in new location
            var tempFile: File = File(contexts.getExternalCacheDir().toString() + "/ExtractedApk")
            //If directory doesn't exists create new
            if (!tempFile.isDirectory) if (!tempFile.mkdirs()) return
            //rename apk file to app name
            tempFile =
                File(tempFile.path + "/" + (app.labelRes.toString()).replace(" ", "") + ".apk")
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return
                }
            }
            //Copy file to new location
            val inp: InputStream = FileInputStream(originalApk)
            val out: OutputStream = FileOutputStream(tempFile)
            val buf = ByteArray(1024)
            var len: Int
            while (inp.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            inp.close()
            out.close()
            //Open share dialog
            val intent = Intent(Intent.ACTION_SEND)
//MIME type for apk, might not work in bluetooth sahre as it doesn't support apk MIME type

            intent.type = "application/vnd.android.package-archive"
            intent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context, BuildConfig.APPLICATION_ID + ".provider", File(tempFile.path)
                )
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            contexts.startActivity(intent)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun copyStream(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    fun openFile() {
//        val absolutePath = contexts.getExternalCacheDir()?.path;
//        var data = contexts.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        var datas = Environment.getExternalStorageDirectory()
//        if (data != null) {
//            Log.v("------------data",data.path)
//            Log.v("------------data",datas.path)
//        }
        var file: File = File(contexts.getExternalCacheDir().toString() + "/abc.pdf")
        val open = contexts.assets.open("abc.pdf")
        val fileOutputStream: FileOutputStream;
        try {
            fileOutputStream = FileOutputStream(file)
            copyStream(open, fileOutputStream)
            fileOutputStream.close()
            open.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // create new Intent
        // create new Intent
        val intent = Intent(Intent.ACTION_VIEW)

// set flag to give temporary permission to external app to use your FileProvider

// set flag to give temporary permission to external app to use your FileProvider
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

// generate URI, I defined authority as the application ID in the Manifest, the last param is file I want to open

// generate URI, I defined authority as the application ID in the Manifest, the last param is file I want to open
        val uri: Uri? = FileProvider.getUriForFile(
            this.contexts,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

// I am opening a PDF file so I give it a valid MIME type

// I am opening a PDF file so I give it a valid MIME type
        intent.setDataAndType(uri, "application/pdf")
        contexts.startActivity(intent)

// validate that the device can open your File!

// validate that the device can open your File!

        val pm: PackageManager = contexts.getPackageManager()
        if (intent.resolveActivity(pm) != null) {
            contexts.startActivity(intent)
        }

    }
}