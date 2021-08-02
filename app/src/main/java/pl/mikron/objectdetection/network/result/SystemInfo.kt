package pl.mikron.objectdetection.network.result

import android.os.Build

data class SystemInfo(

    var manufacturer: String = Build.MANUFACTURER,

    var model: String = Build.MODEL,

    var hardware: String = Build.HARDWARE,

    var board: String = Build.BOARD,

    var apiLevel: Int = Build.VERSION.SDK_INT
)
