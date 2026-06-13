package com.androidify.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Androidify.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class AndroidifyApp : Application()
