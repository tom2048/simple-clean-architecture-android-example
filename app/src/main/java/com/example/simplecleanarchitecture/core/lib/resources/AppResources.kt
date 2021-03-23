package com.example.simplecleanarchitecture.core.lib.resources

import android.app.Application

interface AppResources {
    fun getStringResource(id: Int): String
    fun getIntResource(id: Int): Int
}

class AppResourcesDefault(val application: Application) : AppResources {

    override fun getStringResource(id: Int): String = application.getString(id)

    override fun getIntResource(id: Int): Int = application.resources.getInteger(id)

}