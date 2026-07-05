package me.danny.shop.tracking

import me.danny.shop.utils.color
import me.danny.shop.utils.hex
import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.concurrent.TimeUnit
import kotlin.math.round


object Graph {
    fun create(
        width: Int = 54,
        height: Int = 14,
        dataByDay: List<List<Long>>,
        timescale: TimeUnit
    ): List<String> {
//        if (points.all { it == 0L }) {
//            return listOf("&7&oNever sold.".color())
//        }

        val axisBorder = "&7▇"
        val blankColorInactive = "#242230".hex()
        val blankColorActive = "#3c3a4a".hex()
        val blank = "╱"
        val full = "▇"
        val peak = "▃"

        val days = dataByDay.size
        val daysPerDiv = days / width.toDouble()
        val chunks = (days + width - 1) / width
        val points = dataByDay.chunked(chunks)
            .map { group -> group.sumOf(List<Long>::sum) }
            .toMutableList()
        while (points.size < width) {
            points.addFirst(0)
        }

        val yDiv = points.max() / height.toDouble()
        val yLegendStep = height / 3
        val graph = height.downTo(0).map { y ->
            val line = (0..width).joinToString("") { x ->
                if (y == 0 || x == 0) axisBorder
                else {
                    val pointIdx = x - 1
                    if (pointIdx > points.lastIndex) {
                        return@joinToString blank
                    }

                    val bin = points[pointIdx]

                    val color = interpolate("#dd0000".hex(), "#00ff00".hex(), y.toDouble() / height)
                    if (bin > yDiv * y) "$color$full"
                    else if (bin > (yDiv * (y - 0.5))) "$color$peak"
                    else {
                        if (y % yLegendStep == 0) "$blankColorActive$blank"
                        else "$blankColorInactive$blank"
                    }
                }
            }

            val suffix =
                if (y == height) "&6${fmtToAbbreviated(points.max().toDouble())}".color()
                else if (y == 0) "&70".color()
                else if (y % yLegendStep == 0) "&7${fmtToAbbreviated(y * yDiv)}".color()
                else "${"#1c1c1c".hex()}${fmtToAbbreviated(y * yDiv)}".color()
            "$line $suffix".color()
        }.toMutableList()

        graph.add("&7&oX Scale: ${round(daysPerDiv * 100.0) / 100.0} ${timescale.name.lowercase()} per division.".color())
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
        var output = ""
        for ((minimum, abbr) in abbreviatedUnits) {
            if (x >= minimum) {
                output = abbr.format.format(round(x) / abbr.baseQuantity)
            } else break
        }

        if (output.isBlank()) output = "${round(x * 100.0) / 100.0}"
        return output
    }
}

fun interpolate(start: ChatColor, end: ChatColor, percent: Double): ChatColor {
    val startColor = start.color
    val endColor = end.color

    val newR = (startColor.red + (endColor.red - startColor.red) * percent).toInt()
    val newG = (startColor.green + (endColor.green - startColor.green) * percent).toInt()
    val newB = (startColor.blue + (endColor.blue - startColor.blue) * percent).toInt()

    return ChatColor.of(Color(newR, newG, newB))
}