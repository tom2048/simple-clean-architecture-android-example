package com.example.simplecleanarchitecture.core.repository

import android.app.Application
import android.net.Uri
import io.reactivex.rxjava3.core.Single
import java.io.FileNotFoundException
import java.io.InputStream

interface StorageRepository {
    fun load(url: String): Single<ByteArray>
}

class FileStorageRepository(private val application: Application) : StorageRepository {

    override fun load(url: String): Single<ByteArray> = Single.create { emitter ->
        var inputStream: InputStream? = null
        try {
            inputStream = application.contentResolver.openInputStream(Uri.parse(url))
            emitter.onSuccess(inputStream?.readBytes())
        } catch (e: FileNotFoundException) {
            emitter.onError(e)
        } finally {
            inputStream?.close()
        }
    }

}

