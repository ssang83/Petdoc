package kr.co.petdoc.petdoc.db.care.scanImage.ear

import android.os.Parcel
import android.os.Parcelable

/**
 * Petdoc
 * Class: ImageItem
 * Created by kimjoonsung on 2020/06/26.
 *
 * Description :
 */
data class EarItem(
    var id: Long? = null,
    var petId: Int = -1,
    var year: String? = "",
    var month: String? = "",
    var day: String? = "",
    var time: String? = "",
    var leftImagePath: String? = "",
    var leftImage: ByteArray? = null,
    var rightImagePath: String? = "",
    var rightImage: ByteArray? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readValue(Long::class.java.classLoader) as Long?,
        source.readInt(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.createByteArray(),
        source.readString(),
        source.createByteArray()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeValue(id)
        writeInt(petId)
        writeString(year)
        writeString(month)
        writeString(day)
        writeString(time)
        writeString(leftImagePath)
        writeByteArray(leftImage)
        writeString(rightImagePath)
        writeByteArray(rightImage)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EarItem> = object : Parcelable.Creator<EarItem> {
            override fun createFromParcel(source: Parcel): EarItem = EarItem(source)
            override fun newArray(size: Int): Array<EarItem?> = arrayOfNulls(size)
        }
    }
}
