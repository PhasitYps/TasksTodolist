package com.chillchillapp.gthingstodo.master

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources

import android.os.Build
import java.util.*


class LanguageConfig {

    fun changeLanguage(context: Context, languageCode: String): ContextWrapper? {
        var context: Context = context
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        val systemLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {
            configuration.locale
        }
        if (languageCode != "" && !systemLocale.language.equals(languageCode)) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
            } else {
                configuration.locale = locale
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            }
        }
        return ContextWrapper(context)
    }
}