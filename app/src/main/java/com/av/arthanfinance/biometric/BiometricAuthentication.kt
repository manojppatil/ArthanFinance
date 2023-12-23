package com.nttdata.mobillitysecurityframework.biometric

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.nttdata.mobillitysecurityframework.msfcomponents.MSFCallback
import java.util.concurrent.Executors

class BiometricAuthentication {
    companion object {

        private lateinit var biometricPrompt: BiometricPrompt

        fun startScanning(
            context: FragmentActivity,
            title: String,
            subtitle: String,
            description: String,
            MSFCallback: MSFCallback
        ): String {
            var errorStr: String
            biometricPrompt = BiometricPrompt(
                context,
                Executors.newSingleThreadExecutor(),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        MSFCallback.onFailure(errString as String)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        MSFCallback.onSuccess("Authentication successful")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        MSFCallback.onFailure("Authentication Failure")
                    }
                })

            var promptInfo = createPromptInfo(title, subtitle, description)
            if (promptInfo != null) {
                biometricPrompt.authenticate(promptInfo)
            }
        return ""
        }
        //Create the BiometricPrompt instance//
        fun createPromptInfo(
            title: String,
            subtitle: String,
            description: String
        ): BiometricPrompt.PromptInfo? {
            return BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setConfirmationRequired(true)
//                .setNegativeButtonText(Utility.negativeButtonText)
                .setDeviceCredentialAllowed(true)
                .build()
        }
    }
}