package org.example.rabbi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbiApplication

fun main(args: Array<String>) {
    runApplication<RabbiApplication>(*args)
}
