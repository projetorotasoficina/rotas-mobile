package br.edu.utfpr.geocoleta

import android.app.Application
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient

class GeoColetaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrovitClient.initialize(this)
    }
}