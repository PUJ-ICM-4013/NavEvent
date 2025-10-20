package com.example.naveventapp.data

import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.*

object DirectionsService {
    private val client = OkHttpClient()

    /**
     * Llama a Directions API (modo walking por defecto) y retorna la lista de puntos de la polyline
     * del overview (suficiente para pintar la ruta).
     */
    fun fetchRoutePolyline(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        mode: String = "walking"
    ): List<LatLng> {
        val url =
            "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=$mode&key=$apiKey"

        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val body = resp.body?.string() ?: return emptyList()

            val root = JSONObject(body)
            val routes = root.optJSONArray("routes") ?: return emptyList()
            if (routes.length() == 0) return emptyList()

            val route = routes.getJSONObject(0)
            val overview = route.optJSONObject("overview_polyline") ?: return emptyList()
            val encoded = overview.optString("points", "")
            if (encoded.isBlank()) return emptyList()

            return decodePolyline(encoded)
        }
    }

    /** Decodificador estándar de Google Polyline (no requiere libs extra). */
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
