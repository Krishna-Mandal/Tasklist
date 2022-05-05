package tasklist

import kotlinx.datetime.*
import kotlin.system.exitProcess

const val ADD = "add"
const val PRINT = "print"
const val END = "end"
const val EDIT = "edit"
const val DELETE = "delete"

val APRIORIST = listOf("C", "H", "N", "L")

enum class VALIDFIELDS(s: String) {
    PRIORITY("priority"),
    DATE("date"),
    TIME("time"),
    TASK("task");

    companion object {
        fun isValidEnum(value: String): Boolean {
            return try {
                VALIDFIELDS.valueOf(value.uppercase())
                true
            } catch (except: Exception) {
                false
            }
        }
    }
}

fun main() {
    perFormTask()
}

fun getTaskPriority(): String {
    var prio: String
    do {
        println("Input the task priority (C, H, N, L):")
        prio = readln().uppercase()
    } while (prio !in APRIORIST)

    return prio
}

fun getDate(): String {
    var repeat: Boolean
    var date = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
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

    return date.toString()
}

fun getTime(): String {
    var repeat: Boolean
    val date = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
    var time = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0"))
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

    return time.toString().split("T").last()
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
            val prio = getTaskPriority()
            val date = getDate()
            val time = getTime()
            addTask(taskList, date, time, prio)
        }
        PRINT -> printTaskList(taskList)
        END -> {
            println("Tasklist exiting!")
            exitProcess(0)
        }
        EDIT -> editTask(tasks = taskList)
        DELETE -> deleteTask(tasks = taskList)
        else -> println("The input action is invalid")
    }
}

fun printTaskList(tasks: MutableMap<String, List<String>>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        var number = 0
        tasks.forEach{ (mapKey, taskList) ->
            println("${++number}${numOfSpaces(number)}$mapKey")
            taskList.forEach{
                println("   $it")
            }
            println()
        }
    }
}

fun addTask(tasks: MutableMap<String, List<String>>, date: String, time: String, prio: String) {
    println("Input a new task (enter a blank line to end):")
    val mapKey = "$date $time $prio ${getDueTag(date)}"
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
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        printTaskList(tasks)
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

fun editTask(tasks: MutableMap<String, List<String>>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        println("Input the task number (1-${tasks.size}):")
        val taskNumber = readln().trim()
        val key = getKeys(tasks.keys.toList(), taskNumber)

        if(key == "INVALID") {
            println("Invalid task number")
        } else {
            println("Input a field to edit (priority, date, time, task):")
            val field = readln()
            do {
                if (VALIDFIELDS.isValidEnum(field)) {
                    println("Invalid field")
                } else {
                    editTaskByType(tasks, VALIDFIELDS.valueOf(field), key)
                }
            } while (VALIDFIELDS.isValidEnum(field))
            println("The task is changed")
        }
    }
}

fun getDueTag(date: String): String {
    val yrMnDay = date.split("-").map { it.toInt() }
    val taskDate = LocalDate(yrMnDay.first(), yrMnDay[1], yrMnDay.last())
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
    val numberOfDays = currentDate.daysUntil(taskDate)

    return when {
        numberOfDays == 0 -> "T"
        numberOfDays > 0 -> "I"
        else -> "O"
    }
}

fun editTaskByType(tasks: MutableMap<String, List<String>>, fieldType: VALIDFIELDS, key: String) {
    val oldKeys = key.split(" ").toMutableList()
    val value: List<String>? = tasks[key]
    when(fieldType) {
        VALIDFIELDS.DATE -> {
            oldKeys[1] = getDate()
            oldKeys[4] = getDueTag(oldKeys[1])
            tasks.remove(key)
            if (value != null) {
                tasks[oldKeys.joinToString (separator = " ")] = value
            }
        }
        VALIDFIELDS.TIME -> {
            oldKeys[2] = getTime()
            tasks.remove(key)
            if (value != null) {
                tasks[oldKeys.joinToString (separator = " ")] = value
            }
        }
        VALIDFIELDS.PRIORITY -> {
            oldKeys[3] = getTaskPriority()
            tasks.remove(key)
            if (value != null) {
                tasks[oldKeys.joinToString (separator = " ")] = value
            }
        }
        else -> {
            tasks.remove(key)
            addTask(tasks, oldKeys[1], oldKeys[2], oldKeys[3])
        }
    }
}

fun getKeys(keys: List<String>, number: String): String {
    val isDigit = number.toCharArray().all { it.isDigit() }

    if(!isDigit) {
        return "INVALID"
    }

    if (number.toInt() >= keys.size) {
        return "INVALID"
    }

    return keys[number.toInt()]
}

fun numOfSpaces(index: Int): String {
    return if (index >= 10) {
        " "
    } else {
        "  "
    }
}


