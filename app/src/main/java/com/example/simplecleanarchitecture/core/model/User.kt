package com.example.simplecleanarchitecture.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String?,
    val nickname: String,
    val email: String,
    val description: String = "",
    val photo: ByteArray? = null,
    val idScan: ByteArray? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id == other.id
            && nickname == other.nickname
            && email == other.email
            && description == other.description
            && photo.contentEquals(other.photo)
            && idScan.contentEquals(other.idScan)
        ) {
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result: Int = id?.hashCode() ?: 0
        result = result * 31 + (nickname?.hashCode() ?: 0)
        result = result * 31 + (email?.hashCode() ?: 0)
        result = result * 31 + (description?.hashCode() ?: 0)
        result = result * 31 + (photo?.contentHashCode() ?: 0)
        result = result * 31 + (idScan?.contentHashCode() ?: 0)
        return result
    }
}