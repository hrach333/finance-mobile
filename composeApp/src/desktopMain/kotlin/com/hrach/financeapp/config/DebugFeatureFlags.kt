package com.hrach.financeapp.config

actual object DebugFeatureFlags {
    actual val onboardingResetButtonEnabled: Boolean
        get() = java.lang.Boolean.getBoolean("smartbudget.onboardingReset")
}
