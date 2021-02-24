package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PetSepciesData
 * Created by kimjoonsung on 2020/07/03.
 *
 * Description :
 */
data class PetSpeciesData(
    var id: Int,
    var kind: String,
    var name: String
)