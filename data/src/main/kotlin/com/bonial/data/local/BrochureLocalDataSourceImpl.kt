package com.bonial.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrochureLocalDataSourceImpl
    @Inject
    constructor(
        private val dao: BrochuresDao,
    ) : BrochureLocalDataSource {
        override suspend fun getCachedBrochures(): List<BrochureEntity> = dao.getAll()

        override suspend fun cacheBrochures(brochures: List<BrochureEntity>) {
            dao.deleteAll()
            dao.insertAll(brochures)
        }
    }
