package com.amazon.ivs.realtimecollab.core.handlers

import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.common.launchDefault
import com.amazon.ivs.realtimecollab.core.common.launchMain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

const val BOTTOM_SHEET_ANIMATION_DURATION = 300

/**
 * Might as well be a singleton ViewModel that you inject in all composables with hilt, but given the nature
 * of the project and the fact that there won't be any unit tests - this is much more convenient.
 * Also - no need to include the Navigation lib (v3? v4? Who know what version it has at this point at reading)
 * and this gives us much more flexibility and full control over every aspect of the navigation.
 */
object NavigationHandler {
    private val _backStack = mutableListOf<Destination>()
    private val _destination = MutableStateFlow<Destination>(Destination.None)
    private val _bottomSheetDestination = MutableStateFlow<BottomSheetDestination>(BottomSheetDestination.None)
    private val _errorDestination = MutableStateFlow<ErrorDestination>(ErrorDestination.None)
    private val _isBottomSheetClosing = MutableStateFlow(false)
    private val _loadingState = MutableStateFlow(LoadingState.None)
    private var _errorJob: Job? = null
    private var _hideBottomSheetJob: Job? = null

    val destination = _destination.asStateFlow()
    val bottomSheetDestination = _bottomSheetDestination.asStateFlow()
    val errorDestination = _errorDestination.asStateFlow()
    val isBottomSheetClosing = _isBottomSheetClosing.asStateFlow()
    val loadingState = _loadingState.asStateFlow()

    init {
        _loadingState.update { LoadingState.Splash }
        AuthHandler.checkSession(notifyOnError = false)
    }

    fun goTo(destination: Destination) {
        val indexOfExactCopy = _backStack.indexOfFirst { it == destination }.takeIf { it >= 0 }
        val indexOfClassInstance = _backStack.indexOfFirst { it::class == destination::class }.takeIf { it >= 0 }
        if (indexOfClassInstance != null) {
            Timber.d("Reordered the: $destination from: $indexOfClassInstance to ${_backStack.size - 1}")
            _backStack.removeAt(indexOfClassInstance)
        } else if (indexOfExactCopy != null) {
            Timber.d("Reordered the: $destination from: $indexOfExactCopy to ${_backStack.size - 1}")
            _backStack.removeAt(indexOfExactCopy)
        }

        _backStack.add(destination)
        Timber.d("Going to: $destination, ${_backStack.size}")
        _destination.update { destination }

        hideLoading()
        hideError()
        hideBottomSheet()
    }

    fun goBack() {
        val currentBottomSheet = _bottomSheetDestination.value
        val currentDestination = _destination.value

        if (currentBottomSheet !is BottomSheetDestination.None) {
            hideBottomSheet()
            return
        }

        if (currentDestination is Destination.JoinScreen || currentDestination is Destination.StageScreen) {
            val resetToHomeScreen = currentDestination is Destination.StageScreen
            StageHandler.leaveStage(resetToHomeScreen = resetToHomeScreen)
            if (resetToHomeScreen) return
        }

        val last = _backStack.removeLastOrNull()
        val parent = _backStack.lastOrNull() ?: Destination.Finish

        Timber.d("Returning to: $parent from: $last")
        _destination.update { parent }
        if (parent is Destination.Finish) {
            launchDefault {
                delay(300)
                _destination.update { last ?: Destination.SignInScreen }
            }
        }
    }

    fun reset() {
        Timber.d("Resetting backstack")
        hideLoading()
        hideError()
        hideBottomSheet()
        _backStack.clear()
        _destination.update { Destination.None }
        _bottomSheetDestination.update { BottomSheetDestination.None }
    }

    fun showError(error: Int, hasAction: Boolean = false) {
        Timber.d("Showing error: $error, hasAction: $hasAction")
        if (hasAction) {
            _errorDestination.update { ErrorDestination.Toast(error = error, hasAction = true) }
            return
        }

        _errorJob?.cancel()
        _errorJob = launchMain {
            _errorDestination.update { ErrorDestination.Toast(error = error, hasAction = false) }
            delay(5000)
            _errorDestination.update { ErrorDestination.None }
            _errorJob = null
        }
    }

    fun hideError() {
        Timber.d("Hiding error")
        _errorJob?.cancel()
        _errorJob = null
        _errorDestination.update { ErrorDestination.None }
    }

    fun showLoading(loadingState: LoadingState) {
        Timber.d("Showing loading: $loadingState")
        _loadingState.update { loadingState }
    }

    fun hideLoading() {
        Timber.d("Hiding loading")
        _loadingState.update { LoadingState.None }
    }

    fun showBottomSheet(destination: BottomSheetDestination) {
        Timber.d("Showing bottom sheet: $destination")
        _bottomSheetDestination.update { destination }
    }

    fun hideBottomSheet() {
        if (_bottomSheetDestination.value is BottomSheetDestination.None) return
        if (_hideBottomSheetJob != null) return

        StageHandler.closeBottomSheets()
        _hideBottomSheetJob = launchMain {
            _isBottomSheetClosing.update { true }
            delay(BOTTOM_SHEET_ANIMATION_DURATION.toLong())

            Timber.d("Closing bottom sheet")
            _bottomSheetDestination.update { BottomSheetDestination.None }

            delay(BOTTOM_SHEET_ANIMATION_DURATION.toLong())
            _isBottomSheetClosing.update { false }
            _hideBottomSheetJob = null
        }
    }
}

enum class LoadingState {
    None,
    Splash,
    Loading,
}

sealed class Destination {
    data object None : Destination()
    data object Finish : Destination()
    data object HomeScreen : Destination()
    data object JoinScreen : Destination()
    data object SignInScreen : Destination()
    data object SignUpScreen : Destination()
    data object StageScreen : Destination()
}

sealed class BottomSheetDestination {
    data object None : BottomSheetDestination()
    data object Members : BottomSheetDestination()
    data object Chat : BottomSheetDestination()
    data object Settings : BottomSheetDestination()
    data object SignOut : BottomSheetDestination()
}

sealed class ErrorDestination {
    open var error: Int = R.string.err_generic
    open var hasAction: Boolean = false

    data object None : ErrorDestination()
    data class Toast(
        override var error: Int,
        override var hasAction: Boolean
    ) : ErrorDestination()
}
