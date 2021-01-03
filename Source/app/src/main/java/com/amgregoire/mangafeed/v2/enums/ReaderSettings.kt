package com.amgregoire.mangafeed.v2.enums

enum class ReaderSettings(val pageCountCache: Int, val imageSizeFactor: Double) {
    Max(6, 2.25),
    Medium(3, 1.0),
    Low(1, 0.5)
}