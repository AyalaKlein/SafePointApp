package models

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.*

@Serializable
data class Shelter(val id: Int, val locY: Double, val locX: Double, val description: String) {}