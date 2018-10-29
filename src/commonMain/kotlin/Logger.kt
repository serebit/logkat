package com.serebit.logkat

import com.serebit.logkat.formatting.FormatterPayload
import com.serebit.logkat.formatting.TimestampGenerator
import com.serebit.logkat.writers.ConsoleWriter
import com.serebit.logkat.writers.MessageWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * The main logging class, through which messages are processed and sent to an output vector. This object can be
 * configured at runtime, extended, and instantiated.
 */
open class Logger : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private var timestampGenerator = TimestampGenerator("yyyy-MM-dd HH:mm:ss")
    /**
     * Convenience variable for setting the pattern of the timestamp sent to the [formatter].
     */
    var timestampPattern: String
        get() = timestampGenerator.pattern
        set(value) {
            timestampGenerator.pattern = value
        }
    /**
     * Determines whether logs should be written asynchronously via coroutines. While this does provide significant
     * performance improvements, logs just before a program exit may not be written, so this defaults to false.
     */
    var async: Boolean = false
    /**
     * The [LogLevel] from which the logger will output log messages. Defaults to [LogLevel.WARNING].
     */
    var level: LogLevel = LogLevel.WARNING
    /**
     * The log message formatter.
     */
    var formatter: (FormatterPayload) -> String = { (time, threadName, level, message) ->
        "$time [$threadName] $level: $message"
    }
    /**
     * The [MessageWriter] that will be used to output log messages. Can be any predefined MessageWriter, or a custom
     * implementation.
     */
    var writer: MessageWriter = ConsoleWriter()

    /**
     * Logs a [message] with the level [TRACE][LogLevel.TRACE].
     *
     * @param message The message to log.
     */
    fun trace(message: String) = log(LogLevel.TRACE, message)

    /**
     * Logs a [message] with the level [DEBUG][LogLevel.DEBUG].
     *
     * @param message The message to log.
     */
    fun debug(message: String) = log(LogLevel.DEBUG, message)

    /**
     * Logs a [message] with the level [INFO][LogLevel.INFO].
     *
     * @param message The message to log.
     */
    fun info(message: String) = log(LogLevel.INFO, message)

    /**
     * Logs a [message] with the level [WARNING][LogLevel.WARNING].
     *
     * @param message The message to log.
     */
    fun warn(message: String) = log(LogLevel.WARNING, message)

    /**
     * Logs a [message] with the level [ERROR][LogLevel.ERROR].
     *
     * @param message The message to log.
     */
    fun error(message: String) = log(LogLevel.ERROR, message)

    /**
     * Logs a [message] with the level [FATAL][LogLevel.FATAL].
     *
     * @param message The message to log.
     */
    fun fatal(message: String) = log(LogLevel.FATAL, message)

    private fun log(level: LogLevel, message: String) = when {
        // check if the message should actually be logged
        this.level > level -> Unit
        // write the log within a coroutine if async is enabled
        async -> {
            launch { writeLog(level, message) }
            Unit
        }
        // otherwise, just run it on the current thread
        else -> writeLog(level, message)
    }

    private fun writeLog(level: LogLevel, message: String) {
        // example: 2018-01-12 21:03:25 [main] INFO: Logged Message
        FormatterPayload(timestampGenerator, level, message)
            .let(formatter)
            .let { formattedMessage ->
                (writer as? ConsoleWriter)?.write(formattedMessage, level) ?: writer.write(formattedMessage)
            }
    }

    companion object : Logger()
}