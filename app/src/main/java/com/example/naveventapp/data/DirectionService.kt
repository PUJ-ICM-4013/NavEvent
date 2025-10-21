package com.example.naveventapp.data

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

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

    // ---------- NUEVO: Roads API (opcional) ----------
    private suspend fun nearestRoadPoint(p: LatLng, apiKey: String): LatLng? =
        withContext(Dispatchers.IO) {
            val param = "${p.latitude},${p.longitude}"
            val url = "https://roads.googleapis.com/v1/nearestRoads?points=$param&key=$apiKey"
            runCatching {
                client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val body = resp.body?.string().orEmpty()
                    if (body.isBlank()) return@withContext null
                    val root = JSONObject(body)
                    val snaps = root.optJSONArray("snappedPoints") ?: return@withContext null
                    if (snaps.length() == 0) return@withContext null
                    val loc = snaps.getJSONObject(0).optJSONObject("location") ?: return@withContext null
                    LatLng(loc.optDouble("latitude"), loc.optDouble("longitude"))
                }
            }.getOrNull()
        }

    // ---------- EXISTENTE: Directions ----------
    suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        mode: String = "walking"
    ): RouteResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null
        fun LatLng.asParam(): String = "%.${6}f,%.${6}f".format(java.util.Locale.US, latitude, longitude)

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

                val r0 = routes.getJSONObject(0)
                val legs = r0.optJSONArray("legs")
                val leg0 = legs?.optJSONObject(0)
                val distObj = leg0?.optJSONObject("distance")
                val durObj  = leg0?.optJSONObject("duration")

                val encoded = r0.optJSONObject("overview_polyline")?.optString("points").orEmpty()
                val pts = if (encoded.isBlank()) emptyList() else decodePolyline(encoded)

                RouteResult(
                    points = pts,
                    distanceMeters = distObj?.optInt("value") ?: 0,
                    durationSeconds = durObj?.optInt("value") ?: 0,
                    distanceText = distObj?.optString("text").orEmpty(),
                    durationText = durObj?.optString("text").orEmpty()
                )
            }
        }.getOrNull()
    }

    // ---------- NUEVO: ruta “inteligente” con snap + fallback ----------
    suspend fun fetchSmartRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        mode: String = "walking"
    ): List<LatLng> {
        // 1) intenta ruta directa
        fetchRoute(origin, destination, apiKey, mode)?.points?.let { if (it.size >= 2) return it }

        // 2) prueba snap a vías (si Roads no está habilitada, devolverá null y seguimos)
        val oSnap = nearestRoadPoint(origin, apiKey)
        val dSnap = nearestRoadPoint(destination, apiKey)

        // 2a) si hay al menos un snap, arma ruta combinada
        if (oSnap != null || dSnap != null) {
            val o2 = oSnap ?: origin
            val d2 = dSnap ?: destination
            val mid = fetchRoute(o2, d2, apiKey, mode)?.points.orEmpty()

            if (mid.size >= 2) {
                val pre = if (oSnap != null) listOf(origin, oSnap) else emptyList()
                val post = if (dSnap != null) listOf(dSnap, destination) else emptyList()
                return pre + mid + post
            }
        }

        // 3) último recurso: línea recta
        return listOf(origin, destination)
    }

    // ---------- decode ----------
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0; var lng = 0
        while (index < len) {
            var b: Int; var shift = 0; var result = 0
            do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1); lat += dlat
            shift = 0; result = 0
            do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1); lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
}
