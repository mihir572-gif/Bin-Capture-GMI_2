
package com.gmitrading.bincapture.util

object CsvUtil {
    fun parseLine(line: String): List<String> {
        // Simple CSV parser (supports quoted fields)
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i+1] == '"') {
                        cur.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(cur.toString())
                    cur = StringBuilder()
                }
                else -> cur.append(c)
            }
            i++
        }
        result.add(cur.toString())
        return result
    }
}
