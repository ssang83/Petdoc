package kr.co.petdoc.petdoc.network.response.submodel
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PetInfoItem
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description : 펫 정보 아이템
 */
data class PetInfoItem(
    var ageType: Int,
    var birthday: String?,
    var cmInfoId: Int?,
    var gender: String?,
    var id: Int,
    var imageUrl: String?,
    var inoculationStatus: String?,
    var isNeutralized: Boolean,
    var kind: String?,
    var name: String?,
    var needUpdate: Boolean,
    var regInfoAllStep: Int,
    var regInfoStep: Int,
    var regPetStep: Int,
    var species: String?,
    var speciesId: Int,
    var userId: Int,
    var animalId:Int,
    var existCmResult:Boolean

) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString(),
        source.readValue(Int::class.java.classLoader) as Int?,
        source.readString(),
        source.readInt(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        source.readInt(),
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readInt(),
        source.readInt(),
        source.readInt(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(ageType)
        writeString(birthday)
        writeValue(cmInfoId)
        writeString(gender)
        writeInt(id)
        writeString(imageUrl)
        writeString(inoculationStatus)
        writeInt((if (isNeutralized) 1 else 0))
        writeString(kind)
        writeString(name)
        writeInt((if (needUpdate) 1 else 0))
        writeInt(regInfoAllStep)
        writeInt(regInfoStep)
        writeInt(regPetStep)
        writeString(species)
        writeInt(speciesId)
        writeInt(userId)
        writeInt(animalId)
        writeInt((if (existCmResult) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PetInfoItem> = object : Parcelable.Creator<PetInfoItem> {
            override fun createFromParcel(source: Parcel): PetInfoItem = PetInfoItem(source)
            override fun newArray(size: Int): Array<PetInfoItem?> = arrayOfNulls(size)
        }
    }
}