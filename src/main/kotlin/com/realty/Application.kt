package com.realty

import com.realty.data.DataSeeder
import com.realty.plugins.configureDatabase
import com.realty.plugins.configureRouting
import com.realty.plugins.configureSecurity
import com.realty.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    configureDatabase()
    DataSeeder.seedIfEmpty()
    configureSecurity()
    configureRouting()
}
