package com.example.onboardflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class OnboardflowApplication

fun main(args: Array<String>) {
    runApplication<OnboardflowApplication>(*args)
}
