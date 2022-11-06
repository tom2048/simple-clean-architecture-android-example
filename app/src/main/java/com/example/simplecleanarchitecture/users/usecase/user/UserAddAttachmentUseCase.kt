package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.StorageRepository
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCaseDefault.Type
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCaseDefault.Type.Avatar
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCaseDefault.Type.IdScan
import io.reactivex.rxjava3.core.Single

interface UserAddAttachmentUseCase : (String, String, Type) -> Single<String>

class UserAddAttachmentUseCaseDefault(
    private val storageRepository: StorageRepository,
    private val assetsRepository: AssetsRepository
) : UserAddAttachmentUseCase {

    override fun invoke(userId: String, url: String, type: Type): Single<String> =
        storageRepository.load(url)
            .flatMap { data ->
                when (type) {
                    Avatar -> AssetsRepository.AVATAR_ID_PATTERN.format(userId)
                    IdScan -> AssetsRepository.ID_SCAN_ID_PATTERN.format(userId)
                }.let { key ->
                    assetsRepository.saveImage(key, data).andThen(Single.just(key))
                }
            }

    enum class Type {
        Avatar, IdScan
    }
}