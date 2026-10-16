package io.github.beppe2880.opencodemobile

import io.github.beppe2880.opencodemobile.core.data.di.dataModule
import io.github.beppe2880.opencodemobile.fake.fakeDataModule
import org.koin.core.module.Module

/** Debug builds use the real data layer unless built with `-PfakeData=true`. */
fun dataModules(): List<Module> = if (BuildConfig.USE_FAKE_DATA) listOf(fakeDataModule) else listOf(dataModule)
