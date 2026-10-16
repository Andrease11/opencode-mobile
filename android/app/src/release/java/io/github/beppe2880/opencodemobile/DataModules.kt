package io.github.beppe2880.opencodemobile

import io.github.beppe2880.opencodemobile.core.data.di.dataModule
import org.koin.core.module.Module

/** Release builds always talk to a real server. */
fun dataModules(): List<Module> = listOf(dataModule)
