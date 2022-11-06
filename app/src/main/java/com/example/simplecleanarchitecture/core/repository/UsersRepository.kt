package com.example.simplecleanarchitecture.core.repository

import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.*
import java.util.concurrent.TimeUnit

interface UsersRepository {
    fun getList(): Observable<List<UserDetails>>
    fun get(id: String): Observable<UserDetails>
    fun insert(user: UserDetails): Single<String>
    fun update(user: UserDetails): Completable
    fun updatePassword(userId: String, password: String): Completable
    fun delete(id: String): Completable
}

/**
 * Implementation of UsersRepository
 * It's only a simple memory implementation for this example project, in real project it would be a REST or database repository.
 * Timers were added for real data source delay simulation.
 */
class UsersRepositoryMemory(private val appSchedulers: AppSchedulers) : UsersRepository {

    private val users = mutableMapOf<String, UserWithCredentials>().apply {
        // Test users
        put(
            "a312b3ee-84c2-11eb-8dcd-0242ac130003",
            UserWithCredentials(
                UserDetails(
                    "a312b3ee-84c2-11eb-8dcd-0242ac130003",
                    "Nickname1",
                    "nickname1@test.com",
                    "Test description 1"
                )
            )
        )
        put(
            "3b04aacf-4320-48bb-8171-af512aae0894",
            UserWithCredentials(
                UserDetails(
                    "3b04aacf-4320-48bb-8171-af512aae0894",
                    "Nickname2",
                    "nickname2@test.com",
                    "Test description 2"
                )
            )
        )
        put(
            "52408bc4-4cdf-49ef-ac54-364bfde3fbf0",
            UserWithCredentials(
                UserDetails(
                    "52408bc4-4cdf-49ef-ac54-364bfde3fbf0",
                    "Nickname3",
                    "nickname3@test.com",
                    "Test description 3"
                )
            )
        )
    }

    override fun getList(): Observable<List<UserDetails>> {
        synchronized(users) {
            return Observable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                .map { users.values.toList().map { it.user } }
                .subscribeOn(appSchedulers.io())
        }
    }

    override fun get(id: String): Observable<UserDetails> {
        synchronized(users) {
            return users[id]?.let {
                val user = it.user
                Observable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .map { user }
                    .subscribeOn(appSchedulers.io())
            } ?: run {
                Observable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .flatMap { Observable.error<UserDetails>(Exception("User not found")) }
                    .subscribeOn(appSchedulers.io())
            }
        }
    }

    override fun insert(user: UserDetails): Single<String> {
        synchronized(users) {
            return if (user.id.isNullOrEmpty()) {
                val id = UUID.randomUUID().toString()
                users[id] = UserWithCredentials(user.copy(id = id))
                Single.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .flatMap { Single.just(id) }
                    .subscribeOn(appSchedulers.io())
            } else {
                Single.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .flatMap { Single.error<String>(Exception("Invalid user object")) }
                    .subscribeOn(appSchedulers.io())
            }
        }
    }

    override fun update(user: UserDetails): Completable {
        synchronized(users) {
            return user.id?.let { id ->
                users[id] = UserWithCredentials(user)
                Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .subscribeOn(appSchedulers.io())
            } ?: run {
                Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .andThen(Completable.error(Exception("Invalid user object")))
                    .subscribeOn(appSchedulers.io())
            }
        }
    }

    override fun updatePassword(userId: String, password: String): Completable {
        synchronized(users) {
            return users[userId]?.let { user ->
                users[userId] = user.copy(password = password)
                Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .subscribeOn(appSchedulers.io())
            } ?: run {
                Completable.timer(TEST_DELAY_MILLIS, TimeUnit.MILLISECONDS)
                    .andThen(Completable.error(Exception("Invalid user id")))
                    .subscribeOn(appSchedulers.io())
            }
        }
    }

    override fun delete(id: String): Completable {
        synchronized(users) {
            return if (users.remove(id) != null) {
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
        private const val TEST_DELAY_MILLIS = 300L
    }

    private data class UserWithCredentials(
        val user: UserDetails,
        val password: String? = null
    )
}