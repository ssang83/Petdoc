package kr.co.petdoc.petdoc.network.response.submodel
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HospitalItem
 * Created by kimjoonsung on 2020/05/26.
 *
 * Description :
 */
data class RelatedHospitalItem(
    val addition: List<String>? = null,
    val address: String? = null,
    val counsel: String? = null,
    val distance: Double,
    val holiday: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("is_kaha")
    val isKaha: Boolean,
    @SerializedName("is_svma")
    val isSvma: Boolean,
    val kinds: String? = null,
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
    val pk: Int,
    @SerializedName("service_hour")
    val serviceHour: String? = null,
    val telephone: String? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        source.createStringArrayList(),
        source.readString(),
        source.readString(),
        source.readDouble(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        1 == source.readInt(),
        source.readString(),
        source.readDouble(),
        source.readDouble(),
        source.readString(),
        source.readInt(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeStringList(addition)
        writeString(address)
        writeString(counsel)
        writeDouble(distance)
        writeString(holiday)
        writeString(imageUrl)
        writeInt((if (isKaha) 1 else 0))
        writeInt((if (isSvma) 1 else 0))
        writeString(kinds)
        writeDouble(latitude)
        writeDouble(longitude)
        writeString(name)
        writeInt(pk)
        writeString(serviceHour)
        writeString(telephone)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RelatedHospitalItem> = object : Parcelable.Creator<RelatedHospitalItem> {
            override fun createFromParcel(source: Parcel): RelatedHospitalItem = RelatedHospitalItem(source)
            override fun newArray(size: Int): Array<RelatedHospitalItem?> = arrayOfNulls(size)
        }
    }
}