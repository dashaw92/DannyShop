package me.danny.shop.utils

import kotlin.math.pow

object MathParser {

    data class MathException(override val message: String) : Exception()

    sealed interface ExprTree {
        data class Add(val left: ExprTree, val right: ExprTree) : ExprTree
        data class Sub(val left: ExprTree, val right: ExprTree) : ExprTree
        data class Mult(val left: ExprTree, val right: ExprTree) : ExprTree
        data class Divide(val left: ExprTree, val right: ExprTree) : ExprTree
        data class Exp(val left: ExprTree, val power: ExprTree) : ExprTree
        data class Literal(val literal: Double) : ExprTree
        data class Ident(val ident: String) : ExprTree
        data class UnaryPlus(val expr: ExprTree) : ExprTree
        data class UnaryMinus(val expr: ExprTree) : ExprTree

        companion object {
            fun eval(expr: ExprTree, bindings: Map<String, Number>): Double {
                val boundEval = { tree: ExprTree -> eval(tree, bindings) }
                return when (expr) {
                    is Add -> boundEval(expr.left) + boundEval(expr.right)
                    is Sub -> boundEval(expr.left) - boundEval(expr.right)
                    is Mult -> boundEval(expr.left) * boundEval(expr.right)
                    is Divide -> {
                        val right = boundEval(expr.right)
                        if (right == 0.0) throw MathException("Divide by 0: ${expr.right}")
                        boundEval(expr.left) / right
                    }

                    is Exp -> boundEval(expr.left).pow(boundEval(expr.power))
                    is Literal -> expr.literal
                    is Ident -> bindings[expr.ident]?.toDouble() ?: throw MathException("Missing binding for ${expr.ident}")
                    is UnaryPlus -> +boundEval(expr.expr)
                    is UnaryMinus -> -boundEval(expr.expr)
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
                    left = if (eat('+')) ExprTree.Add(left, parseTerm())
                    else if (eat('-')) ExprTree.Sub(left, parseTerm())
                    else return left
                }
            }

            // Handles multiplication and division
            fun parseTerm(): ExprTree {
                var left = parseFactor()
                while (true) {
                    left = if (eat('*')) ExprTree.Mult(left, parseFactor())
                    else if (eat('/')) ExprTree.Divide(left, parseFactor())
                    else return left
                }
            }

            // Handles parentheses, numbers, and exponents
            fun parseFactor(): ExprTree {
                if (eat('+')) return ExprTree.UnaryPlus(parseFactor())
                if (eat('-')) return ExprTree.UnaryMinus(parseFactor())

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

                if (eat('^')) left = ExprTree.Exp(left, parseFactor())

                return left
            }
        }.parse()
    }
}