package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ElevatorUseCase(val scope: CoroutineScope) {
    val elevatorCount = 1
    val levels = 10
    val currentLevelOfUser = 0
    val elevators = mutableListOf<ElevatorState>()

    init {
        repeat(elevatorCount) { index ->
            elevators.add(ElevatorState(index + 1))
        }

        printElevatorState()
    }

    private fun printElevatorState() {
        scope.launch {
            while (true) {
                delay(1000)
                val line = elevators.joinToString(separator = " | ") { elevator ->
                    "Elevator-${elevator.id}: ${if (elevator.isMoving) "MOVING" else "IDLE"}-${elevator.level}"
                }
                println(line)
            }
        }
    }

    suspend fun handleInput(whereToGo: Int) {
        val elevatorState = elevators.filter { !it.isMoving }.minByOrNull { it.level - currentLevelOfUser }
        println("Idle Elevator: ${elevatorState?.id}")
        elevatorState?.let {
            if (currentLevelOfUser == elevatorState.level) {
                assignElevator(whereToGo, it)
            } else if (currentLevelOfUser < elevatorState.level) {
                bringElevator(currentLevelOfUser, it)
                assignElevator(whereToGo, it)
            }
        }
    }

    fun assignElevator(whereToGo: Int, elevator: ElevatorState) {
        scope.launch {
            while (elevator.level < whereToGo) {
                elevator.isMoving = true
                delay(2000)
                elevator.level++
            }
            elevator.isMoving = false
        }
    }

    suspend fun bringElevator(whereToGo: Int, elevator: ElevatorState) {
        scope.launch {
            while (elevator.level > whereToGo) {
                elevator.isMoving = true
                delay(2000)
                elevator.level--
            }
            elevator.isMoving = false
        }.join()
    }
}