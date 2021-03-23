package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import io.reactivex.rxjava3.core.Single

interface UserShowDetailsUseCase : (String) -> Single<User>

class UserShowDetailsUseCaseDefault(private val usersRepository: UsersRepository, private val assetsRepository: AssetsRepository) : UserShowDetailsUseCase {

    // No unit tests for simple getters
    override fun invoke(id: String): Single<User> = usersRepository
        .get(id)
        .firstOrError()
        .flatMap { user ->
            assetsRepository.getImage(user.id!!).map { image ->
                User(user.id, user.nickname, user.email, user.description, image)
            }.onErrorReturn {
                User(user.id, user.nickname, user.email, user.description, null)
            }
        }

}