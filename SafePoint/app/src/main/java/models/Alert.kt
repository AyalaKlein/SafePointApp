package models

import kotlinx.serialization.*
import org.joda.time.DateTime

@Serializable
data class Alert(@Serializable(with= DateSerializer::class)val alertDate: DateTime, val alertSeconds: Int)

@Serializer(forClass = DateSerializer::class)
object DateSerializer : KSerializer<DateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("DateSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(value.millis.toString())
    }

    override fun deserialize(decoder: Decoder): DateTime {
        return DateTime(decoder.decodeString().toLong())
    }
}