package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import io.reactivex.rxjava3.core.Completable

interface UserUpdateUseCase : (User) -> Completable

class UserUpdateUseCaseDefault(
    private val usersRepository: UsersRepository,
    private val assetsRepository: AssetsRepository,
    private val appResources: AppResources
) : UserUpdateUseCase {

    override fun invoke(user: User): Completable {
        // TODO: It would be possible to create validation in this place instead of in the viewModel, which could be more convenient when reusing use case or
        //  simply when performing client - server consistent validation. TBD
        // Regarding validation - there is some useful discussion about:
        // https://stackoverflow.com/questions/57603422/clean-architecture-where-to-put-input-validation-logic
        // https://groups.google.com/g/clean-code-discussion/c/latn4x6Zo7w/m/bFwtDI1XSA8J
        //val validationErrors = mutableListOf<Pair<String, String>>()
        //if (!Patterns.EMAIL_ADDRESS.matcher(user.email).matches()) {
        //    validationErrors.add(Pair(Validation.EMAIL_VALIDATION_KEY, appResources.getStringResource(R.string.email_validation_message)))
        //}
        //val isValid = validationErrors.isEmpty()
        val isValid = true
        return if (isValid) {
            val userDetails = UserDetails(user.id, user.nickname, user.email, user.description)
            if (user.id.isNullOrEmpty()) {
                usersRepository.insert(userDetails)
                    .flatMapCompletable { id ->
                        user.photo?.let {
                            assetsRepository.saveImage(id, user.photo)
                        } ?: run {
                            Completable.complete()
                        }
                    }
            } else {
                usersRepository.update(userDetails)
                    .andThen(user.photo?.let {
                        assetsRepository.saveImage(user.id, user.photo)
                    } ?: run {
                        Completable.complete()
                    })
            }
        } else {
            // TODO: Possible validation
            //Completable.error(ValidationException(validationErrors))
            Completable.error(ValidationException(listOf()))
        }
    }

}