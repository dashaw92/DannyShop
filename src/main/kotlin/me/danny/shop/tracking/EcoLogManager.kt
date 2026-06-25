package me.danny.shop.tracking

import me.danny.shop.DannyShop
import me.danny.shop.model.ID
import me.danny.shop.model.Item
import java.io.*
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


private fun now(): ZonedDateTime = ZonedDateTime.now()

private fun generateLogfileDate(zdt: ZonedDateTime = now()): String = zdt.format(
    DateTimeFormatter.ofPattern("LLL-yyyy")
)

private fun logfileName(zdt: ZonedDateTime = now()): String = "sales-${generateLogfileDate(zdt)}.csv.gz"
private val initialLogName = logfileName() //used to filter out the current log so it's not loaded as historical
private fun formatDateTime(zdt: ZonedDateTime): String = zdt.format(DateTimeFormatter.ISO_DATE_TIME)

data class SaleRecord(
    val time: ZonedDateTime,
    val seller: UUID,
    val item: ID,
    val amount: Long,
    val price: BigDecimal,
    val ext: BigDecimal
) {
    companion object {
        internal fun fromCSV(csv: String): SaleRecord? {
            val parts = csv.split(",")
            try {
                val time = ZonedDateTime.parse(parts[0], DateTimeFormatter.ISO_DATE_TIME)
                val seller = UUID.fromString(parts[1])
                val item = parts[2]
                val amount = parts[3].toLong()
                val price = BigDecimal(parts[4])
                val ext = BigDecimal(parts[5])

                return SaleRecord(time, seller, ID(item), amount, price, ext)
            } catch (_: Exception) {
                if (!csv.isEmpty()) {
                    DannyShop.instance().logger.warning("[Logging] Invalid record, skipping: \"$csv\"")
                }
                return null
            }
        }
    }

    constructor(seller: UUID, item: Item, amount: Long, ext: BigDecimal) : this(
        now(),
        seller,
        item.iid,
        amount,
        BigDecimal((item.cost as Item.Cost.Value).buy),
        ext
    )

    internal fun toCSV(): String =
        "${formatDateTime(time)},$seller,${item.id},$amount,${price.toDouble()},${ext.toDouble()}"
}


internal object EcoLogMgr {

    internal data class EcoLog(
        val zdt: ZonedDateTime,
        val records: MutableList<SaleRecord>,
        val readonly: Boolean = false
    )

    private val baseDir = DannyShop.instance().dataFolder.resolve("logs/")

    fun availableLogs(): List<String> = baseDir.list().toList().filter { name -> name != initialLogName  }

    fun loadHistorical(name: String): EcoLog? {
        if (name == initialLogName) return (DannyShop.instance().ecolog as EcoLogging).currentLog
        val file = baseDir.resolve(name)
        if (file.absoluteFile.parentFile != baseDir.absoluteFile) {
            //do not permit any directory traversal.
            return null
        }

        val records = try {
            GZIPInputStream(FileInputStream(file)).bufferedReader()
                .use(BufferedReader::readText)
                .lines()
                .mapNotNull(SaleRecord::fromCSV)
                .toMutableList()
        } catch (_: FileNotFoundException) {
            return null
        }

        return EcoLog(now(), records, true)
    }

    fun load(date: ZonedDateTime = now()): EcoLog {
        baseDir.mkdirs()
        val file = baseDir.resolve(logfileName(date))

        val records = try {
            GZIPInputStream(FileInputStream(file)).bufferedReader()
                .use(BufferedReader::readText)
                .lines()
                .mapNotNull(SaleRecord::fromCSV)
                .toMutableList()
        } catch (_: FileNotFoundException) {
            mutableListOf()
        }

        return EcoLog(date, records)
    }

    fun save(log: EcoLog) {
        if (log.readonly) return

        baseDir.mkdirs()
        val file = baseDir.resolve(logfileName(log.zdt))

        try {
            GZIPOutputStream(FileOutputStream(file)).bufferedWriter()
                .use { bw ->
                    log.records.forEach {
                        bw.write(it.toCSV())
                        bw.newLine()
                    }
                }
        } catch (ex: IOException) {
            DannyShop.instance().logger.severe("An error occurred trying to save sale logs.")
            ex.printStackTrace()
        }
    }
}