package io.github.developrofthings.kespl.packet.data.alert

/**
 * This class represents the data for a single alert detected by a Valentine One..
 *
 * @property bytes The raw byte array containing the alert data.
 */
data class AlertData(private val bytes: ByteArray) {

    /**
     * Returns the byte at the specified index in the alert data.
     */
    operator fun get(index: Int): Byte = bytes[index]

    /**
     * The alert index & count for this alert.
     */
    val alertIndexCount: AlertIndexCount get() = AlertIndexCount(this[ALERT_INDEX_CNT_INDEX])

    /**
     * The frequency in MHz of this alert.
     */
    val frequency: Int get() = ((this[FREQ_MSB_IDX].toInt() and 0xFF) shl 8) or (this[FREQ_LSB_IDX].toInt() and 0xFF)

    /**
     * The detected signal strength in the front for this alert.
     */
    val frontSignalStrength: Int get() = (this[FRONT_SIGNAL_STRENGTH_IDX].toInt() and 0xFF)

    /**
     * The detected signal strength in the rear for this alert.
     */
    val rearSignalStrength: Int get() = (this[REAR_SIGNAL_STRENGTH_IDX].toInt() and 0xFF)

    /**
     * The band arrow definition for this alert.
     */
    val bandArrowDefinition: BandArrow get() = BandArrow(this[BAND_ARROW_DEF_IDX])

    /**
     * The aux0 definition for this alert.
     */
    val aux: Aux0 get() = Aux0(this[AUX_IDX])

    /**
     * Indicates if the alert has the highest priority in the alert table.
     */
    val isPriority: Boolean get() = aux.priorityAlert

    /**
     * Indicates if the alert has been determined to be false alert and will be removed from
     * subsequent alert tables.
     *
     * @since V4.1032
     */
    val isJunk: Boolean get() = aux.junkAlert

    /**
     * The [AlertBand] of this alert; does not include [PhotoRadar] ie [AlertBand.Photo] will not be
     * returned.
     */
    val band: AlertBand get() = bandArrowDefinition.band

    /**
     * The direction, in respect to the Valentine One's rear antenna, this alert was detected.
     */
    val arrow: AlertArrow get() = bandArrowDefinition.arrow

    /**
     * Photo Radar type of the current alert
     *
     * @since V4.1037
     */
    val photoRadar: PhotoRadar get() = aux.photoRadarType

    fun getAlertBand(supportsPhoto: Boolean): AlertBand = if (supportsPhoto) band else getPhotoAwareBand()

    /**
     * The [AlertBand] of this alert; includes photo radar types.
     */
    fun getPhotoAwareBand(): AlertBand {
        if (band == AlertBand.K) {
            if (aux.photoRadarType != PhotoRadar.None) return AlertBand.Photo
        }
        return band
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AlertData

        if (!bytes.contentEquals(other.bytes)) return false
        if (frequency != other.frequency) return false
        if (frontSignalStrength != other.frontSignalStrength) return false
        if (rearSignalStrength != other.rearSignalStrength) return false
        if (isPriority != other.isPriority) return false
        if (isJunk != other.isJunk) return false
        if (alertIndexCount != other.alertIndexCount) return false
        if (bandArrowDefinition != other.bandArrowDefinition) return false
        if (aux != other.aux) return false
        if (band != other.band) return false
        if (arrow != other.arrow) return false
        if (photoRadar != other.photoRadar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + frequency
        result = 31 * result + frontSignalStrength
        result = 31 * result + rearSignalStrength
        result = 31 * result + isPriority.hashCode()
        result = 31 * result + isJunk.hashCode()
        result = 31 * result + alertIndexCount.hashCode()
        result = 31 * result + bandArrowDefinition.hashCode()
        result = 31 * result + aux.hashCode()
        result = 31 * result + band.hashCode()
        result = 31 * result + arrow.hashCode()
        result = 31 * result + photoRadar.hashCode()
        return result
    }
}

internal const val ALERT_INDEX_CNT_INDEX: Int = 0
internal const val FREQ_MSB_IDX: Int = 1
internal const val FREQ_LSB_IDX: Int = 2
internal const val FRONT_SIGNAL_STRENGTH_IDX: Int = 3
internal const val REAR_SIGNAL_STRENGTH_IDX: Int = 4
internal const val BAND_ARROW_DEF_IDX: Int = 5
internal const val AUX_IDX: Int = 6