package app.lantext.sms

/** A handset echo can be stamped earlier than the send. Keep that copy just after the send. */
const val SELF_ECHO_WINDOW_MS = 180_000L

fun <T> orderEchoes(
    messages: List<T>,
    id: (T) -> String,
    threadId: (T) -> String,
    body: (T) -> String,
    timestamp: (T) -> Long,
    incoming: (T) -> Boolean,
): List<T> {
    if (messages.size < 2) {
        return messages.sortedWith(compareBy<T> { timestamp(it) }.thenBy { incoming(it) })
    }
    val sortAt = HashMap<String, Long>(messages.size)
    for (message in messages) sortAt[id(message)] = timestamp(message)
    val claimed = HashSet<String>()
    val outgoing = messages.filter { !incoming(it) }.sortedBy { timestamp(it) }
    for (sent in outgoing) {
        val text = body(sent).trim()
        if (text.isEmpty()) continue
        val sentAt = timestamp(sent)
        val thread = threadId(sent)
        val echo = messages.asSequence()
            .filter { candidate ->
                val key = id(candidate)
                incoming(candidate) &&
                    key !in claimed &&
                    threadId(candidate) == thread &&
                    body(candidate).trim() == text &&
                    sentAt - timestamp(candidate) in 0..SELF_ECHO_WINDOW_MS
            }
            .minByOrNull { timestamp(it) } ?: continue
        claimed.add(id(echo))
        sortAt[id(echo)] = sentAt + 1
    }
    return messages.sortedWith(compareBy<T>({ sortAt[id(it)] ?: timestamp(it) }, { incoming(it) }))
}

/** Store an incoming echo just after the matching send. Leaves other timestamps alone. */
fun echoStoredTimestamp(incoming: Long, matchingSentAt: List<Long>): Long {
    val match = matchingSentAt
        .filter { sent -> sent >= incoming && sent - incoming <= SELF_ECHO_WINDOW_MS }
        .maxOrNull()
    return if (match != null) match + 1 else incoming
}
