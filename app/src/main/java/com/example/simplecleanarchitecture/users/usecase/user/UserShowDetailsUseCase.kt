package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import io.reactivex.rxjava3.core.Single

interface UserShowDetailsUseCase : (String) -> Single<User>

class UserShowDetailsUseCaseDefault(private val usersRepository: UsersRepository, private val assetsRepository: AssetsRepository) : UserShowDetailsUseCase {

    override fun invoke(id: String): Single<User> = usersRepository
        .get(id)
        .firstOrError()
        .map { user ->
            User(user.id, user.nickname, user.email, user.description)
        }
        .flatMap { user ->
            assetsRepository.getImage(AssetsRepository.AVATAR_ID_PATTERN.format(user.id!!)).map { image ->
                user.copy(photo = image)
            }.onErrorReturn { user }
        }
        .flatMap { user ->
            assetsRepository.getImage(AssetsRepository.ID_SCAN_ID_PATTERN.format(user.id!!)).map { image ->
                user.copy(idScan = image)
            }.onErrorReturn { user }
        }

}