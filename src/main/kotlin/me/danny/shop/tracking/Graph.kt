package me.danny.shop.tracking

import me.danny.shop.utils.color
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.round

object Graph {
    fun create(
        width: Int = 90,
        height: Int = 14,
        points: List<Long>,
        resolution: Long,
        timescale: TimeUnit
    ): List<String> {
//        if (points.all { it == 0L }) {
//            return listOf("&7&oNever sold.".color())
//        }

        val axisBorder = "&7▇"
        val blank = "&8╱"
        val full = "&2▇"
        val peak = "&a▃"

        val binsPerDiv = (1 / width.toDouble()) * points.size.toDouble()

        val yDiv = points.max() / height.toDouble()
        val graph = height.downTo(0).map { y ->
            val line = (0..width).joinToString("") { x ->
                if (y == 0 || x == 0) axisBorder
                else {
                    val bin = points[floor((x - 1) * binsPerDiv).toInt()]

                    if (bin > yDiv * y) full
                    else if (bin > (yDiv * (y - 0.5))) peak
                    else blank
                }
            }

            val suffix =
                if (y == height) "&6${fmtToAbbreviated(points.max().toDouble())}".color()
                else if (y == 0) "&70".color()
                else if (y % 5 == 0) "&7${fmtToAbbreviated(y * yDiv)}".color()
                else ""
            "$line $suffix".color()
        }.toMutableList()

        graph.add("&7&oX Scale: ${round((resolution * binsPerDiv) * 100.0) / 100.0} ${timescale.name.lowercase()} per division.".color())
        graph.add("&7&oY Scale: ${fmtToAbbreviated(yDiv)} units sold per division.".color())
        return graph
    }

    private data class Abbreviation(val baseQuantity: Double, val format: String)

    private val abbreviatedUnits = mapOf(
        1_000.0 to Abbreviation(1_000.0, "%,.2fk"), //thousand decimal
        100_000.0 to Abbreviation(1_000.0, "%,.0fk"), //hundred thousand, no decimal
        1e6 to Abbreviation(1e6, "%,.2fm"), //million decimal
        1e8 to Abbreviation(1e6, "%,.0fm"), //hundred million no decimal
        1e9 to Abbreviation(1e9, "%,.2fb"), //billion decimal
        1e11 to Abbreviation(1e9, "%,.0fb"), //hundred billion no decimal
    )

    internal fun fmtToAbbreviated(x: Double): String {
        var output: String = ""
        for ((minimum, abbr) in abbreviatedUnits) {
            if (x >= minimum) {
                output = abbr.format.format(round(x) / abbr.baseQuantity)
            } else break
        }

        if (output.isBlank()) output = "${round(x * 100.0) / 100.0}"
        return output
    }
}