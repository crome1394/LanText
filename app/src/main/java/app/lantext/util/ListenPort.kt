package app.lantext.util

object ListenPort {
    const val MIN = 1_024
    const val MAX = 65_535

    fun isValid(port: Int): Boolean = port in MIN..MAX

    fun parse(raw: String): Int? {
        val n = raw.trim().toIntOrNull() ?: return null
        return n.takeIf { isValid(it) }
    }
}
