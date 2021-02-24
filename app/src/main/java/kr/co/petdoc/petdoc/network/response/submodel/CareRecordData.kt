package kr.co.petdoc.petdoc.network.response.submodel
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: CareItemData
 * Created by kimjoonsung on 2020/06/22.
 *
 * Description :
 */
data class CareRecordData(
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("log_date")
    var logDate: String?,
    var pk: Int,
    var type: String?,
    var value: String?

) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readInt(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(createdAt)
        writeString(logDate)
        writeInt(pk)
        writeString(type)
        writeString(value)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CareRecordData> =
            object : Parcelable.Creator<CareRecordData> {
                override fun createFromParcel(source: Parcel): CareRecordData =
                    CareRecordData(source)

                override fun newArray(size: Int): Array<CareRecordData?> = arrayOfNulls(size)
            }
    }
}