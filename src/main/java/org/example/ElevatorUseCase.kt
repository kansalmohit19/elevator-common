package org.example

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ElevatorUseCase(val scope: CoroutineScope) {
    val elevatorCount = 4
    val levels = 10
    val currentLevelOfUser = 0
    private val _elevators = MutableStateFlow<List<Elevator>>(emptyList())
    val elevators = _elevators.asStateFlow()

    init {
        repeat(elevatorCount) { index ->
            _elevators.value += Elevator(id = index + 1, displayName = "A")
        }

        printElevatorState()
    }

    private fun printElevatorState() {
        scope.launch {
            while (true) {
                delay(1000)
                val line = _elevators.value.joinToString(separator = " | ") { elevator ->
                    "Elevator-${elevator.id}: ${elevator.stateDisplayText()}-${elevator.level}"
                }
                Log.e("Elevator", line)
            }
        }
    }

    fun handleInput(whereToGo: Int) {

        val elevator =
            elevators.value.filter { !it.isMoving }.minByOrNull { it.level - currentLevelOfUser }
        Log.e("Elevator", "Assigned Elevator: ${elevator?.id}")
        elevator?.let {
            if (currentLevelOfUser == elevator.level) {
                assignElevator(whereToGo, it)
            } else if (currentLevelOfUser < elevator.level) {
                scope.launch {
                    bringElevator(currentLevelOfUser, it).join()
                    assignElevator(whereToGo, it)
                }
            }
        }
    }

    fun assignElevator(whereToGo: Int, elevator: Elevator) {
        scope.launch {
            var currentLevel = _elevators.value.first { it.id == elevator.id }.level

            while (currentLevel < whereToGo) {
                currentLevel += 1
                _elevators.value = _elevators.value.map {
                    if (it.id == elevator.id) it.copy(
                        state = ElevatorState.MOVING, level = currentLevel
                    ) else it
                }
                delay(1000)
            }
            _elevators.value = _elevators.value.map {
                if (it.id == elevator.id) it.copy(
                    state = ElevatorState.IDLE
                ) else it
            }
        }
    }

    fun bringElevator(whereToGo: Int, elevator: Elevator): Job {
        return scope.launch {
            var currentLevel = _elevators.value.first { it.id == elevator.id }.level

            while (currentLevel > whereToGo) {
                currentLevel -= 1
                _elevators.value = _elevators.value.map {
                    if (it.id == elevator.id) it.copy(
                        state = ElevatorState.MOVING, level = currentLevel
                    ) else it
                }
                delay(1000)
            }
            _elevators.value = _elevators.value.map {
                if (it.id == elevator.id) it.copy(
                    state = ElevatorState.IDLE
                ) else it
            }
        }
    }
}