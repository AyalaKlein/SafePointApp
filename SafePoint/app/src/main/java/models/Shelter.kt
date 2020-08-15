package models

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.*

@Serializable
data class Shelter(val latLong: LatLng, val details: String) {}