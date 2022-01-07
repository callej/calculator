package calculator

import java.math.BigInteger

const val POWER = 15
const val MULT = 13
const val DIV = 13
const val ADD = 11
const val SUB = 11
const val PARENTHESES = 1

val opPrio = mapOf(
    "^" to POWER,
    "*" to MULT,
    "/" to DIV,
    "+" to ADD,
    "-" to SUB,
    "(" to PARENTHESES,
    ")" to PARENTHESES,
)

fun <T> push(stack: MutableList<T>, element: T) {
    stack.add(element)
}

fun <T> pop(stack: MutableList<T>, pos: Int = 1): T {
    val element = stack[stack.size - pos]
    stack.removeAt(stack.size - pos)
    return element
}

fun <T> peek(stack: MutableList<T>, pos: Int = 1): T {
    return stack[stack.size - pos]
}

fun calc(expr: List<String>, vars: MutableMap<String, BigInteger>): String {

    fun doEval(op: String, eval: MutableList<BigInteger>) {
        try {
            when (op) {
                "^" -> push(eval, pop(eval, 2).pow(pop(eval).toInt()))
                "*" -> push(eval, pop(eval) * pop(eval))
                "/" -> push(eval, pop(eval, 2) / pop(eval))
                "+" -> push(eval, pop(eval) + pop(eval))
                "-" -> push(eval, pop(eval, 2) - pop(eval))
                else -> throw Exception("Invalid Expression") // ) is missing
            }
        } catch (e: Exception) {
            throw Exception("Invalid Expression")
        }
    }

    fun evalParentheses(opStack: MutableList<String>, evalStack: MutableList<BigInteger>) {
        try {
            while (peek(opStack) != "(") doEval(pop(opStack), evalStack)
            pop(opStack)
        } catch (e: Exception) {
            throw Exception("Invalid Expression") // ( is missing
        }
    }

    fun addOp(op: String, opStack: MutableList<String>, eval: MutableList<BigInteger>) {
        while (opStack.isNotEmpty() && opPrio[peek(opStack)]!! >= opPrio[op]!!) doEval(pop(opStack), eval)
        push(opStack, op)
    }

    val opStack = emptyList<String>().toMutableList()
    val evalStack = emptyList<BigInteger>().toMutableList()
    for (i in expr.indices) {
        when {
            Regex("[+-]+").matches(expr[i]) ->
                addOp(if (expr[i].count { it == '-' } % 2 == 0) "+" else "-", opStack, evalStack)
            expr[i] == "(" ->
                push(opStack, "(")
            expr[i] == ")" ->
                evalParentheses(opStack, evalStack)
            expr[i] in opPrio.keys ->
                addOp(expr[i], opStack, evalStack)
            Regex("[+-]?\\d+").matches(expr[i]) ->
                push(evalStack, expr[i].toBigInteger())
            expr[i] in vars.keys ->
                push(evalStack, vars[expr[i]]!!)
            expr[i].first() in "+-" && expr[i].substring(1) in vars.keys ->
                push(evalStack, vars[expr[i].substring(1)]!! * BigInteger.valueOf(if (expr[i].first() == '+') 1 else -1))
            Regex("[a-zA-Z]+").matches(expr[i]) ->
                throw Exception("Unknown variable")
            else ->
                throw Exception("Invalid Expression")
        }
    }
    try {
        while (opStack.isNotEmpty()) doEval(pop(opStack), evalStack)
    } catch (e: Exception) {
        throw Exception("Invalid Expression")
    }
    if (evalStack.size != 1) throw Exception("Syntax Error!")
    return pop(evalStack).toString()
}

fun doCommand(input: String) {
    when (input) {
        "/help" -> println("This is a calculator!")
        else -> throw Exception("Unknown Command")
    }
}

fun assignment(input: String, variables: MutableMap<String, BigInteger>) {
    if (!Regex("[a-zA-Z]+ *=.*").matches(input)) {
        throw Exception("Invalid identifier")
    } else if (!Regex("[a-zA-Z]+ *= *[+-]?([a-zA-Z]+|\\d+)( +[()^*/+-]+ +[+-]?([a-zA-Z]+|\\d+))*").matches(input)) {
        throw Exception("Invalid assignment")
    } else {
        val (id, value) = Regex(" *= *").split(input, 2)
        if (Regex("\\d+").matches(value)) {
            variables[id] = value.toBigInteger()
        } else {
            variables[id] = calc(parse(value), variables).toBigInteger()
        }
    }
}

fun parse(expression: String): List<String> {
    var exp = expression
    val expList = mutableListOf<String>()
    while (exp.isNotEmpty()) {
        exp = exp.trim()
        when {
            Regex("^[+-]+ ").containsMatchIn(exp) -> expList.add(Regex("^[+-]+").find(exp)!!.value)
            Regex("^[+-][a-zA-Z]+").containsMatchIn(exp) -> expList.add(Regex("^[+-][a-zA-Z]+").find(exp)!!.value)
            Regex("^[a-zA-Z]+").containsMatchIn(exp) -> expList.add(Regex("^[a-zA-Z]+").find(exp)!!.value)
            Regex("^[+-]?\\d+").containsMatchIn(exp) -> expList.add(Regex("^[+-]?\\d+").find(exp)!!.value)
            exp.first().toString() in opPrio.keys -> expList.add(exp.first().toString())
            else -> throw Exception("Invalid symbol in expression.")
        }
        exp = exp.drop(expList.last().length)
    }
    return expList.toList()
}

fun main() {
    val variables = mutableMapOf<String, BigInteger>()
    while (true) {
        val input = readLine()!!.trim()
        try {
            when {
                input == "" -> {}
                input == "/exit" -> break
                input[0] == '/' -> doCommand(input)
                input.contains('=') -> assignment(input, variables)
                else -> println(calc(parse(input), variables))

            }
         } catch (e: Exception) {
             println(e.message)
         }
    }
    println("Bye!")
}