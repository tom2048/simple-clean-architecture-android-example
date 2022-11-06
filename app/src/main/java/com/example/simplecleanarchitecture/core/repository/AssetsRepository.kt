package com.example.simplecleanarchitecture.core.repository

import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

/**
 * This is repository responsible for providing user assets like images, logos, documents, avatars, etc.
 */
interface AssetsRepository {
    fun getImage(assetId: String): Single<ByteArray>
    fun saveImage(assetId: String, contents: ByteArray): Completable
    fun deleteImage(assetId: String): Completable

    companion object {
        const val AVATAR_ID_PATTERN = "%s-avatar"
        const val ID_SCAN_ID_PATTERN = "%s-id-scan"
    }

}

/**
 * Implementation of asset repository. This is just an example, so for simplicity we have simple memory implementation, but in real life most likely
 * we will need file system based repository here.
 */
class AssetsRepositoryMemory(private val appSchedulers: AppSchedulers) : AssetsRepository {

    private val assets = mutableMapOf<String, ByteArray>()

    override fun getImage(assetId: String): Single<ByteArray> {
        synchronized(assets) {
            return assets[assetId]?.let { image ->
                Single.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .map { image }
                    .subscribeOn(appSchedulers.io())
            } ?: run {
                Single.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .flatMap { Single.error<ByteArray>(Exception("User not found")) }
                    .subscribeOn(appSchedulers.io())
            }
        }
    }

    override fun saveImage(assetId: String, contents: ByteArray): Completable {
        synchronized(assets) {
            assets[assetId] = contents
            return Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .subscribeOn(appSchedulers.io())
        }
    }

    override fun deleteImage(assetId: String): Completable {
        synchronized(assets) {
            return if (assets.remove(assetId) != null) {
                Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .subscribeOn(appSchedulers.io())
            } else {
                Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .andThen(Completable.error(Exception("Invalid user id")))
                    .subscribeOn(appSchedulers.io())
            }
        }
    }

    companion object {
        private const val TEST_DELAY_MILLIS = 1L
    }
}