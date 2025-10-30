package com.amazon.ivs.realtimecollab.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.BottomSheetDestination
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.core.handlers.ErrorDestination
import com.amazon.ivs.realtimecollab.core.handlers.LoadingState
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.core.handlers.StageHandler
import com.amazon.ivs.realtimecollab.ui.components.ErrorBarContent
import com.amazon.ivs.realtimecollab.ui.components.PermissionRequester
import com.amazon.ivs.realtimecollab.ui.components.PreviewSurface
import com.amazon.ivs.realtimecollab.ui.components.ScreenPreview
import com.amazon.ivs.realtimecollab.ui.components.StageScreenPreview
import com.amazon.ivs.realtimecollab.ui.screens.LoadingScreenContent
import com.amazon.ivs.realtimecollab.ui.theme.RealtimeCollabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        super.onCreate(savedInstanceState)
        setContent {
            val destination by NavigationHandler.destination.collectAsStateWithLifecycle()
            val bottomSheetDestination by NavigationHandler.bottomSheetDestination.collectAsStateWithLifecycle()
            val errorDestination by NavigationHandler.errorDestination.collectAsStateWithLifecycle()
            val loadingState by NavigationHandler.loadingState.collectAsStateWithLifecycle()

            RealtimeCollabTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    if (destination is Destination.Finish) {
                        finish()
                        NavigationHandler.reset()
                        return@Scaffold
                    }

                    BackHandler(onBack = NavigationHandler::goBack)
                    PermissionRequester()
                    MainContent(
                        destination = destination,
                        bottomSheetDestination = bottomSheetDestination,
                        errorDestination = errorDestination,
                        loadingState = loadingState,
                        innerPadding = innerPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    destination: Destination,
    bottomSheetDestination: BottomSheetDestination,
    errorDestination: ErrorDestination,
    loadingState: LoadingState,
    innerPadding: PaddingValues,
) {
    MainActivityContent(
        destination = destination,
        innerPadding = innerPadding,
    )
    MainBottomSheetContent(
        bottomSheetDestination = bottomSheetDestination,
        innerPadding = innerPadding,
    )
    ErrorBarContent(
        errorDestination = errorDestination,
        innerPadding = innerPadding,
    )
    LoadingScreenContent(
        loadingState = loadingState,
        innerPadding = innerPadding,
    )
}

@ScreenPreview
@Composable
private fun MainActivitySignInPreview() {
    MainActivityPreview(
        destination = Destination.SignInScreen,
    )
}

@ScreenPreview
@Composable
private fun MainActivitySignInErrorPreview() {
    MainActivityPreview(
        destination = Destination.SignInScreen,
        errorDestination = ErrorDestination.Toast(
            error = R.string.err_sign_in,
            hasAction = true,
        )
    )
}

@ScreenPreview
@Composable
private fun MainActivityLoadingSplashPreview() {
    MainActivityPreview(
        destination = Destination.SignInScreen,
        loadingState = LoadingState.Splash,
    )
}

@ScreenPreview
@Composable
private fun MainActivityLoadingLoadingPreview() {
    MainActivityPreview(
        destination = Destination.SignInScreen,
        loadingState = LoadingState.Loading,
    )
}

@StageScreenPreview
@Composable
private fun MainActivityStageMembersPreview() {
    StageHandler.setPreviewParticipants(count = 6)
    MainActivityPreview(
        destination = Destination.StageScreen,
        bottomSheetDestination = BottomSheetDestination.Members,
    )
}

@Composable
private fun MainActivityPreview(
    destination: Destination,
    bottomSheetDestination: BottomSheetDestination = BottomSheetDestination.None,
    errorDestination: ErrorDestination = ErrorDestination.None,
    loadingState: LoadingState = LoadingState.None,
) {
    PreviewSurface {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            MainContent(
                destination = destination,
                bottomSheetDestination = bottomSheetDestination,
                errorDestination = errorDestination,
                loadingState = loadingState,
                innerPadding = PaddingValues(0.dp),
            )
        }
    }
}
