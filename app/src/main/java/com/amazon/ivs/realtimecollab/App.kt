package com.amazon.ivs.realtimecollab

import android.app.Application
import android.content.Context
import com.amazon.ivs.realtimecollab.core.common.LineNumberDebugTree
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
import timber.log.Timber

/**
 * Given the nature of the project - there is no need for dagger / hilt usage.
 * Having singleton objects instead of repositories / view models in this particular scenario has more benefits
 * than flaws.
 */
lateinit var appContext: Context

class App: Application() {
    override fun onCreate() {
        appContext = this
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }

        initAmplify()
    }

    private fun initAmplify() = try {
        Amplify.addPlugin(AWSCognitoAuthPlugin())
        Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
        Timber.d("Amplify initialized")
    } catch (e: Exception) {
        Timber.e(e, "Failed to init amplify")
    }
}
