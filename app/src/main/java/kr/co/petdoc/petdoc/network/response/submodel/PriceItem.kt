package kr.co.petdoc.petdoc.network.response.submodel
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PriceItem
 * Created by kimjoonsung on 2020/10/05.
 *
 * Description :
 */
data class PriceItem(
    var activeYn: String?,
    var centerId: Int,
    var expenseId: Int,
    var name: String?,
    var price: Int
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(activeYn)
        writeInt(centerId)
        writeInt(expenseId)
        writeString(name)
        writeInt(price)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PriceItem> = object : Parcelable.Creator<PriceItem> {
            override fun createFromParcel(source: Parcel): PriceItem = PriceItem(source)
            override fun newArray(size: Int): Array<PriceItem?> = arrayOfNulls(size)
        }
    }
}