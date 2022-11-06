package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import io.reactivex.rxjava3.core.Single

interface UserGetAttachmentUseCase : (String) -> Single<ByteArray>

class UserGetAttachmentUseCaseDefault(
    private val assetsRepository: AssetsRepository
) : UserGetAttachmentUseCase {

    override fun invoke(key: String): Single<ByteArray> =
        assetsRepository.getImage(key)

}