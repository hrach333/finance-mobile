package com.hrach.financeapp.config

import com.hrach.financeapp.BuildConfig

object FeatureFlags {
    val aiHelpButtonEnabled: Boolean
        get() = BuildConfig.FEATURE_AI_HELP_BUTTON
}
