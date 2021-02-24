package kr.co.petdoc.petdoc.network.response.submodel
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: QuestionData
 * Created by kimjoonsung on 2020/07/01.
 *
 * Description :
 */
data class QuestionData(
    @SerializedName("confirm_content")
    var confirmContent: String?,
    @SerializedName("confirmed_at")
    var confirmedAt: String?,
    var content: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("is_confirm")
    var isConfirm: Boolean,
    var pk: Int,
    var title: String?
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        source.readInt(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(confirmContent)
        writeString(confirmedAt)
        writeString(content)
        writeString(createdAt)
        writeInt((if (isConfirm) 1 else 0))
        writeInt(pk)
        writeString(title)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<QuestionData> = object : Parcelable.Creator<QuestionData> {
            override fun createFromParcel(source: Parcel): QuestionData = QuestionData(source)
            override fun newArray(size: Int): Array<QuestionData?> = arrayOfNulls(size)
        }
    }
}