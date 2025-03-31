package com.nononsenseapps.feeder.truetype

data class TrueTypeMetadata(
    val name: String,
    val family: String,
    val hasWeightVariation: Boolean,
    val hasItalicVariation: Boolean,
)
