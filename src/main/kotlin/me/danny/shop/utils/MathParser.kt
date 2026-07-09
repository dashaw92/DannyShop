package me.danny.shop.utils

import kotlin.math.pow

object MathParser {

    data class MathException(override val message: String) : Exception()

    sealed interface ExprTree {
        data class BinaryOp(val left: ExprTree, val right: ExprTree, val op: (Double, Double) -> Double) : ExprTree
        data class UnaryOp(val expr: ExprTree, val op: (Double) -> Double) : ExprTree
        data class Literal(val literal: Double) : ExprTree
        data class Ident(val ident: String) : ExprTree

        companion object {
            fun eval(expr: ExprTree, bindings: Map<String, Number>): Double {
                val boundEval = { tree: ExprTree -> eval(tree, bindings) }
                return when (expr) {
                    is BinaryOp -> expr.op(boundEval(expr.left), boundEval(expr.right))
                    is UnaryOp -> expr.op(boundEval(expr.expr))
                    is Literal -> expr.literal
                    is Ident -> bindings[expr.ident]?.toDouble() ?: throw MathException("Missing binding for ${expr.ident}")
                }
            }
        }
    }

    fun parse(str: String): ExprTree {
        return object : Any() {
            var pos: Int = -1
            var ch: Int = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat.code) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): ExprTree {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw MathException("Unexpected character: " + ch.toChar())
                return x
            }

            // Handles addition and subtraction
            fun parseExpression(): ExprTree {
                var left = parseTerm()
                while (true) {
                    left = if (eat('+')) ExprTree.BinaryOp(left, parseTerm(), Double::plus)
                    else if (eat('-')) ExprTree.BinaryOp(left, parseTerm(), Double::minus)
                    else return left
                }
            }

            private fun divideThrow(a: Double, b: Double): Double {
                if (b == 0.0) throw MathException("Divide by 0: $a / $b")
                return a / b
            }

            // Handles multiplication and division
            fun parseTerm(): ExprTree {
                var left = parseFactor()
                while (true) {
                    left = if (eat('*')) ExprTree.BinaryOp(left, parseFactor(), Double::times)
                    else if (eat('/')) ExprTree.BinaryOp(left, parseFactor(), ::divideThrow)
                    else if (eat('%')) ExprTree.BinaryOp(left, parseFactor(), Double::mod)
                    else return left
                }
            }

            // Handles parentheses, numbers, and exponents
            fun parseFactor(): ExprTree {
                if (eat('+')) return ExprTree.UnaryOp(parseFactor(), Double::unaryPlus)
                if (eat('-')) return ExprTree.UnaryOp(parseFactor(), Double::unaryMinus)

                val startPos = pos
                var left =
                    if (eat('(')) { // parentheses
                        val expr = parseExpression()
                        if (!eat(')')) throw MathException("Missing closing parenthesis")
                        expr
                    } else if (ch.toChar().isDigit() || ch == '.'.code) { // numbers
                        while (ch.toChar().isDigit() || ch == '.'.code) nextChar()
                        ExprTree.Literal(str.substring(startPos, pos).toDouble())
                    } else if (ch.toChar().isLetter()) {
                        while (ch.toChar().isLetterOrDigit()) nextChar()
                        ExprTree.Ident(str.substring(startPos, pos))
                    } else throw MathException("Unexpected character: " + ch.toChar())

                if (eat('^')) left = ExprTree.BinaryOp(left, parseFactor(), Double::pow)

                return left
            }
        }.parse()
    }
}