package com.example.naveventapp.data

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.*

data class RouteResult(
    val points: List<LatLng>,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val distanceText: String,
    val durationText: String
)

object DirectionsService {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        mode: String = "walking"
    ): RouteResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null

        fun LatLng.asParam(): String =
            "%.${6}f,%.${
                6
            }f".format(locale = java.util.Locale.US, latitude, longitude)

        val url = buildString {
            append("https://maps.googleapis.com/maps/api/directions/json?")
            append("origin=").append(URLEncoder.encode(origin.asParam(), "UTF-8"))
            append("&destination=").append(URLEncoder.encode(destination.asParam(), "UTF-8"))
            append("&mode=").append(mode)
            append("&alternatives=false")
            append("&key=").append(apiKey)
        }

        runCatching {
            client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return@withContext null

                val root = JSONObject(body)
                if (root.optString("status") != "OK") return@withContext null

                val routes = root.optJSONArray("routes") ?: return@withContext null
                if (routes.length() == 0) return@withContext null

                val route0 = routes.getJSONObject(0)
                val legs = route0.optJSONArray("legs")
                val leg0 = legs?.optJSONObject(0)

                val distObj = leg0?.optJSONObject("distance")
                val durObj  = leg0?.optJSONObject("duration")

                val distanceMeters = distObj?.optInt("value") ?: 0
                val durationSeconds = durObj?.optInt("value") ?: 0
                val distanceText = distObj?.optString("text").orEmpty()
                val durationText = durObj?.optString("text").orEmpty()

                val encoded = route0
                    .optJSONObject("overview_polyline")
                    ?.optString("points")
                    .orEmpty()

                val points = if (encoded.isBlank()) listOf(origin, destination) else decodePolyline(encoded)
                RouteResult(points, distanceMeters, durationSeconds, distanceText, durationText)
            }
        }.getOrNull()
    }

    // --- Tu versión suspend de solo polyline sigue como estaba ---
    suspend fun fetchRoutePolyline(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        mode: String = "walking"
    ): List<LatLng> = fetchRoute(origin, destination, apiKey, mode)?.points ?: emptyList()

    // Decodificador igual al que ya tienes
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val latD = lat / 1E5
            val lngD = lng / 1E5
            poly.add(LatLng(latD, lngD))
        }
        return poly
    }
}
