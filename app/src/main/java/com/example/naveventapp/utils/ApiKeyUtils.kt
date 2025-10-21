package com.example.naveventapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle

/**
 * Lee un meta-data string del AndroidManifest.
 * Útil para obtener:
 *  - "com.google.android.geo.API_KEY"  (Maps SDK for Android)
 *  - "com.example.naveventapp.WEB_API_KEY" (Directions/Roads/Geocoding)
 */
fun getMetaDataValue(context: Context, key: String): String {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val bundle: Bundle = appInfo.metaData ?: Bundle.EMPTY
        bundle.getString(key, "")
    } catch (_: Exception) {
        ""
    }
}
