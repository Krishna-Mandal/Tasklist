package tasklist

import kotlinx.datetime.*
import kotlin.system.exitProcess

const val ADD = "add"
const val PRINT = "print"
const val END = "end"
const val EDIT = "edit"
const val DELETE = "delete"

val APRIORIST = listOf("C", "H", "N", "L")

fun main() {
    perFormTask()
}

fun taskPriority(): String {
    var prio: String
    do {
        println("Input the task priority (C, H, N, L):")
        prio = readln().uppercase()
    } while (prio !in APRIORIST)

    return prio
}

fun dateTime(): String {
    var repeat: Boolean
    var date = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
    var time = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0"))
    do {
        println("Input the date (yyyy-mm-dd):")
        repeat = try {
            val yearMonthDay = readln().split("-").map { it.toInt() }
            date = LocalDate(yearMonthDay.first(), yearMonthDay[1], yearMonthDay.last())
            false
        } catch (except: Exception) {
            println("The input date is invalid")
            true
        }
    } while (repeat)

    do {
        println("Input the time (hh:mm):")
        repeat = try {
            val hrMin = readln().split(":").map { it.toInt() }
            time = LocalDateTime(date.year, date.month, date.dayOfMonth, hrMin.first(), hrMin.last())
            false
        } catch (except: Exception) {
            println("The input time is invalid")
            true
        }
    } while (repeat)

    return time.toString().replace("T", " ")
}

fun perFormTask() {
    val taskList = mutableMapOf<String, List<String>>()
    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        validateUserInput(readln().trim(), taskList)
    }
}

fun validateUserInput(command: String, taskList: MutableMap<String, List<String>>) {
    when(command) {
        ADD -> {
            val prio = taskPriority()
            val dateTime = dateTime()
            addTask(taskList, dateTime, prio)
        }
        PRINT -> printTaskList(taskList)
        END -> {
            println("Tasklist exiting!")
            exitProcess(0)
        }
        else -> println("The input action is invalid")
    }
}

fun printTaskList(tasks: MutableMap<String, List<String>>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        tasks.forEach{ (mapKey, taskList) ->
            println(mapKey)
            taskList.forEach{
                println("   $it")
            }
            println()
        }
    }
}

fun addTask(tasks: MutableMap<String, List<String>>, dateTime: String, prio: String) {
    println("Input a new task (enter a blank line to end):")
    val mapKey = "${tasks.size + 1}${numOfSpaces(tasks.size + 1)}$dateTime $prio"
    do {
        val input = readln()
        if (input.isNotBlank()) {
            val list = getTasks(tasks, mapKey)
            list.add(input.trim())
            tasks[mapKey] = list
        } else {
            if (!tasks.containsKey(mapKey)) {
                println("The task is blank")
            }
        }
    } while (input.isNotBlank())
}

fun getTasks(tasks: MutableMap<String, List<String>>, mapKey: String): MutableList<String> {
    return if (tasks.isEmpty() || !tasks.containsKey(mapKey)) {
        mutableListOf()
    } else {
        tasks[mapKey]!!.toMutableList()
    }
}

fun deleteTask(tasks: MutableMap<String, List<String>>) {
    if (tasks.isNullOrEmpty()) {
        println("No tasks have been input")
    } else {
        println("Input the task number (1-${tasks.size}):")
        val taskNumber = readln().trim()
        val key = getKeys(tasks.keys.toList(), taskNumber)

        if(key == "INVALID") {
            println("Invalid task number")
        } else {
            tasks.remove(key)
            println("The task is deleted")
        }
    }
}

fun editTask() {

}

fun getKeys(keys: List<String>, number: String): String {
    val isDigit = number.toCharArray().all { it.isDigit() }

    if(!isDigit) {
        return "INVALID"
    }

    if (keys.map { it.split(" ").first().toInt() }.none { it == number.toInt() }) {
        return "INVALID"
    }

    return keys.groupBy { it.split(" ").first() }[number]!!.first()
}

fun numOfSpaces(index: Int): String {
    return if (index >= 10) {
        " "
    } else {
        "  "
    }
}


