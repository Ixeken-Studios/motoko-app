package com.ixeken.motoko

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de aplicación requerida para inicializar la inyección de dependencias de Hilt.
 */
@HiltAndroidApp
class MotokoApplication : Application()
