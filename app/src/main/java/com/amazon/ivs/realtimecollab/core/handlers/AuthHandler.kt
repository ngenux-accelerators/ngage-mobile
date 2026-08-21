package com.amazon.ivs.realtimecollab.core.handlers

import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.common.LOADING_DELAY
import com.amazon.ivs.realtimecollab.core.common.launchDefault
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.exceptions.SessionExpiredException
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class User(
    val id: String? = null,
    val name: String? = null,
    val token: String? = null,
) {
    val username = name ?: "username"
}

object AuthHandler {
    private val _isError = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _user = MutableStateFlow(User())

    val isError = _isError.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val user = _user.asStateFlow()

    fun signIn(username: String, password: String) {
        if (_isLoading.value) return
        if (username.isBlank() || password.isBlank()) {
            NavigationHandler.showError(error = R.string.err_fields_empty)
            _isError.update { true }
            return
        }

        try {
            _isError.update { false }
            _isLoading.update { true }
            Amplify.Auth.signIn(
                username,
                password,
                { result ->
                    Timber.d("Sign in result: $result")
                    checkSession()
                },
                { error ->
                    Timber.w(error,"Sign in failed: ${error.message}")
                    NavigationHandler.showError(error = R.string.err_sign_in, hasAction = true)
                    _isLoading.update { false }
                }
            )
        } catch (e: Exception) {
            Timber.w(e, "Sign in failed")
            NavigationHandler.showError(error = R.string.err_sign_in, hasAction = true)
            _isLoading.update { false }
        }
    }

    fun signUp(username: String, email: String, password1: String, password2: String) {
        if (_isLoading.value) return
        if (password1.isBlank() || password2.isBlank() || username.isBlank() || email.isBlank()) {
            NavigationHandler.showError(error = R.string.err_fields_empty)
            _isError.update { true }
            return
        }
        if (password1 != password2) {
            NavigationHandler.showError(error = R.string.err_passwords_dont_match)
            _isError.update { true }
            return
        }

        try {
            _isError.update { false }
            _isLoading.update { true }
            Amplify.Auth.signUp(
                username,
                password1,
                AuthSignUpOptions.builder()
                    .userAttribute(AuthUserAttributeKey.email(), email)
                    .build(),
                { result ->
                    Timber.d("Sign up result: $result")
                    checkSession(
                        notifyOnError = false,
                        errorDestination = Destination.SignInScreen,
                    )
                },
                { error ->
                    Timber.w(error,"Sign up failed: ${error.message}")
                    NavigationHandler.showError(error = R.string.err_sign_up, hasAction = true)
                    _isLoading.update { false }
                }
            )
        } catch (e: Exception) {
            Timber.w(e, "Sign up failed")
            NavigationHandler.showError(error = R.string.err_sign_up, hasAction = true)
            _isLoading.update { false }
        }
    }

    fun signOut() = launchDefault {
        if (_isLoading.value) return@launchDefault
        try {
            _isLoading.update { true }
            delay(timeMillis = LOADING_DELAY)
            Amplify.Auth.signOut { result ->
                launchDefault {
                    Timber.d("Signed out: $result")
                    delay(timeMillis = LOADING_DELAY)
                    NavigationHandler.reset()
                    delay(timeMillis = LOADING_DELAY + 100)
                    _isLoading.update { false }
                    NavigationHandler.goTo(Destination.SignInScreen)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Sign out failed")
            NavigationHandler.showError(error = R.string.err_sign_out, hasAction = true)
            _isLoading.update { false }
        }
    }

    fun checkSession(
        notifyOnError: Boolean = true,
        errorDestination: Destination = Destination.SignInScreen,
        errorMessage: Int = R.string.err_sign_in,
    ) = launchDefault {
        try {
            _isLoading.update { true }
            delay(timeMillis = LOADING_DELAY)
            refreshToken(
                onSuccess = { result ->
                    Timber.d("Refreshed auth session result: $result")
                    // TODO: Handle the auth state next step here if needed
                    delay(timeMillis = LOADING_DELAY)
                    NavigationHandler.reset()
                    delay(timeMillis = LOADING_DELAY)

                    _isLoading.update { false }
                    if (result.isSignedIn) {
                        NavigationHandler.goTo(destination = Destination.HomeScreen)
                        return@refreshToken
                    }

                    NavigationHandler.goTo(destination = errorDestination)
                    if (notifyOnError) {
                        NavigationHandler.showError(error = errorMessage, hasAction = true)
                    }
                },
                onError = { error ->
                    Timber.w(error, "Fetch auth session failed: ${error.message}")
                    delay(timeMillis = LOADING_DELAY)
                    _isLoading.update { false }

                    NavigationHandler.goTo(destination = errorDestination)
                    if (notifyOnError) {
                        NavigationHandler.showError(error = errorMessage, hasAction = true)
                    }
                }
            )
        } catch (e: Exception) {
            launchDefault {
                Timber.w(e, "Fetch auth session failed")
                delay(timeMillis = LOADING_DELAY)
                _isLoading.update { false }

                NavigationHandler.goTo(destination = errorDestination)
                if (notifyOnError) {
                    NavigationHandler.showError(error = errorMessage, hasAction = true)
                }
            }
        }
    }

    fun refreshToken(
        onSuccess: suspend (AuthSession) -> Unit = {},
        onError: suspend (AuthException) -> Unit = {},
    ) {
        fun error(error: AuthException?) = launchDefault {
            val exception = error ?: return@launchDefault
            Timber.w(exception, "Failed to refresh auth session")
            onError(exception)
        }

        Amplify.Auth.fetchAuthSession(
            { result ->
                Timber.d("Fetched auth session result: $result")
                val session = result as? AWSCognitoAuthSession
                if (session?.userPoolTokensResult?.error is SessionExpiredException) {
                    Amplify.Auth.signOut {
                        Timber.d("Signed out")
                        NavigationHandler.reset()
                        NavigationHandler.goTo(Destination.SignInScreen)
                        error(error = session.userPoolTokensResult.error)
                    }
                    return@fetchAuthSession
                }
                updateUser(
                    session = session,
                    onUpdated = {
                        onSuccess(result)
                    },
                )
            },
            { error ->
                error(error = error)
            }
        )
    }

    private fun updateUser(
        session: AWSCognitoAuthSession?,
        onUpdated: suspend () -> Unit,
    ) {
        Amplify.Auth.getCurrentUser(
            { user ->
                launchDefault {
                    val user = _user.value.copy(
                        id = user.userId,
                        name = user.username,
                        token = session?.userPoolTokensResult?.value?.idToken,
                    )
                    _user.update { user }
                    Timber.d("Updated user: $user")
                    onUpdated()
                }
            },
            { error ->
                launchDefault {
                    Timber.w(error, "Failed to get current user")
                    onUpdated()
                }
            }
        )
    }
}
