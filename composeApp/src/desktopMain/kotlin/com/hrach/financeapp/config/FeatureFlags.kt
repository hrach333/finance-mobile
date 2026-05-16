package com.hrach.financeapp.config

actual object FeatureFlags {
    actual val aiHelpButtonEnabled: Boolean
        get() = System.getProperty("FEATURE_AI_HELP_BUTTON")?.toBooleanStrictOrNull() ?: false
}
