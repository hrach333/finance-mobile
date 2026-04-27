package com.hrach.financeapp.config

import com.hrach.financeapp.BuildConfig

actual object DebugFeatureFlags {
    actual val onboardingResetButtonEnabled: Boolean
        get() = BuildConfig.DEBUG
}
