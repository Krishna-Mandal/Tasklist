package tasklist

import kotlinx.datetime.*
import kotlin.system.exitProcess

const val ADD = "add"
const val PRINT = "print"
const val END = "end"
const val EDIT = "edit"
const val DELETE = "delete"

const val MAX_CHUNK_SIZE = 44

val APRIORIST = listOf("C", "H", "N", "L")

val PRIOCOLOR = mapOf(
    "C" to "\\u001B[101m \\u001B[0m",
    "H" to "\\u001B[103m \\u001B[0m",
    "N" to "\\u0018[102m \\u001B[0m",
    "L" to "\\u001B[104m \\u001B[0m"
)

val DUECOLOR = mapOf(
    "O" to "\\u001B[101m \\u001B[0m",
    "T" to "\\u001B[103m \\u001B[0m",
    "I" to "\\u0018[102m \\u001B[0m"
)

val EMPTYHEADER = """
    |    |            |       |   |   |""".trimIndent()

val HEADER = """
    +----+------------+-------+---+---+--------------------------------------------+
    | N  |    Date    | Time  | P | D |                   Task                     |
    +----+------------+-------+---+---+--------------------------------------------+""".trimIndent()

val HORIZONTAL = """
    +----+------------+-------+---+---+--------------------------------------------+""".trimIndent()
val VERTICAL = """|"""

enum class VALIDFIELDS {
    PRIORITY,
    DATE,
    TIME,
    TASK;

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
            val (year, month, day) = readln().split("-").map { it.toInt() }
            date = LocalDate(year, month, day)
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
    val taskList = mutableListOf<MutableList<String>>()
    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        validateUserInput(readln().trim(), taskList)
    }
}

fun validateUserInput(command: String, taskList: MutableList<MutableList<String>>) {
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

fun printTaskList(tasks: MutableList<MutableList<String>>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        println(HEADER)
        tasks.forEach{ taskList ->
            var count = 0
            val dateTimePrioDue = taskList.first()
            val tempTaskList  = taskList - taskList.first()
            println(getPrintedRow(number = ++count, dateTimePrioDue = dateTimePrioDue, tasks = tempTaskList))
        }
    }
}

fun getPrintedRow(number: Int, dateTimePrioDue: String, tasks: List<String>): String {
    val (date, time, prio, due) = dateTimePrioDue.split(" ")
    return "$VERTICAL $number${numOfSpaces(number)}$VERTICAL $date $VERTICAL $time $VERTICAL ${PRIOCOLOR[prio]} $VERTICAL ${DUECOLOR[due]} $VERTICAL${getAllTask(tasks)}"
}

fun getAllTask(tasks: List<String>): StringBuilder {
    val taskStr = StringBuilder()

    for (taskIndex in tasks.indices) {
        if (tasks[taskIndex].chunked(MAX_CHUNK_SIZE).size > 1) {
            // Multiline Task
            val chunkedTask = tasks[taskIndex].chunked(MAX_CHUNK_SIZE)
            taskStr.append(
                "${chunkedTask.first()}$VERTICAL".trimIndent()
            )
            for (index in 1 until chunkedTask.size) {
                taskStr.append(
                    "\n$EMPTYHEADER${chunkedTask[index]}$VERTICAL")
            }
        } else {
            if (taskIndex == 0) {
                taskStr.append(
                    "${tasks.first()}${" ".repeat(MAX_CHUNK_SIZE - tasks.first().length)}$VERTICAL")
            } else {
                taskStr.append(
                    "\n$EMPTYHEADER${tasks[taskIndex]}${" ".repeat(MAX_CHUNK_SIZE - tasks[taskIndex].length)}$VERTICAL")
            }
        }
    }
    taskStr.append("\n")
    taskStr.append(HORIZONTAL)
    return taskStr
}

fun addTask(tasks: MutableList<MutableList<String>>, date: String, time: String, prio: String) {
    println("Input a new task (enter a blank line to end):")
    val mapKey = "$date $time $prio ${getDueTag(date)}"
    val tempList = mutableListOf<String>()
    tempList.add(mapKey)
    do {
        val input = readln()
        if (input.isNotBlank()) {
            tempList.add(input.trim())
        } else {
            if (tempList.size == 1) {
                println("The task is blank")
            }
        }
    } while (input.isNotBlank())

    if (tempList.size > 1) {
        tasks.add(tempList)
    }
}

fun deleteTask(tasks: MutableList<MutableList<String>>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        printTaskList(tasks)

        do {
            println("Input the task number (1-${tasks.size}):")
            val taskNumber = readln().trim()
            val isValidKey = validateIndex(tasks.map { it.first() }, taskNumber)
            if(isValidKey == "INVALID") {
                println("Invalid task number")
            } else {
                tasks.removeAt(taskNumber.toInt() - 1)
                println("The task is deleted")
            }
        } while (isValidKey == "INVALID")
    }
}

fun editTask(tasks: MutableList<MutableList<String>>) {
    if (tasks.isEmpty()) {
        println("No tasks have been input")
    } else {
        printTaskList(tasks)

        do {
            println("Input the task number (1-${tasks.size}):")
            val taskNumber = readln().trim()
            val isValidKey = validateIndex(tasks.map { it.first() }, taskNumber)

            if(isValidKey == "INVALID") {
                println("Invalid task number")
            } else {
                do {
                    println("Input a field to edit (priority, date, time, task):")
                    val field = readln().uppercase()
                    if (!VALIDFIELDS.isValidEnum(field)) {
                        println("Invalid field")
                    } else {
                        editTaskByType(tasks, VALIDFIELDS.valueOf(field), taskNumber)
                        println("The task is changed")
                    }
                } while (!VALIDFIELDS.isValidEnum(field))
            }
        } while (isValidKey == "INVALID")
    }
}

fun getDueTag(date: String): String {
    val (year, month, day) = date.split("-").map { it.toInt() }
    val taskDate = LocalDate(year, month, day)
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
    val numberOfDays = currentDate.daysUntil(taskDate)

    return when {
        numberOfDays == 0 -> "T"
        numberOfDays > 0 -> "I"
        else -> "O"
    }
}

fun editTaskByType(tasks: MutableList<MutableList<String>>, fieldType: VALIDFIELDS, key: String) {
    val currentKeyAndValue = tasks[key.toInt() - 1]
    val oldKeys = currentKeyAndValue.first().split(" ").toMutableList()
    val value: List<String> = currentKeyAndValue - currentKeyAndValue.first()
    val tempList = mutableListOf<String>()

    when(fieldType) {
        VALIDFIELDS.DATE -> {
            oldKeys[0] = getDate()
            oldKeys[3] = getDueTag(oldKeys[0])
            tasks.removeAt(key.toInt() - 1)
            tempList.add(oldKeys.joinToString (separator = " "))
            tempList.addAll(value)
            tasks.add(tempList)
        }
        VALIDFIELDS.TIME -> {
            oldKeys[1] = getTime()
            tasks.removeAt(key.toInt() - 1)
            tempList.add(oldKeys.joinToString (separator = " "))
            tempList.addAll(value)
            tasks.add(tempList)
        }
        VALIDFIELDS.PRIORITY -> {
            oldKeys[2] = getTaskPriority()
            tasks.removeAt(key.toInt() - 1)
            tempList.add(oldKeys.joinToString (separator = " "))
            tempList.addAll(value)
            tasks.add(tempList)
        }
        else -> {
            tasks.removeAt(key.toInt() - 1)
            addTask(tasks, oldKeys[0], oldKeys[1], oldKeys[2])
        }
    }
}

fun validateIndex(keys: List<String>, number: String): String {
    val isDigit = number.toCharArray().all { it.isDigit() }

    if(!isDigit) {
        return "INVALID"
    }

    if (number.toInt() > keys.size || number.toInt() <= 0) {
        return "INVALID"
    }

    return "VALID"
}

fun numOfSpaces(index: Int): String {
    return if (index >= 10) {
        " "
    } else {
        "  "
    }
}
