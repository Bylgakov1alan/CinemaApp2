import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("yearWorld") val yearWorld: Int,
    @SerializedName("yearRussian") val yearRussian: Int,
    @SerializedName("descriptionFull") val description: String,
    val plot: String,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("poster_url") val posterUrl: String,
    val genre: String
) : Parcelable