package com.bonial.data.local

interface BrochureLocalDataSource {
    suspend fun getCachedBrochures(): List<BrochureEntity>

    suspend fun cacheBrochures(brochures: List<BrochureEntity>)
}
