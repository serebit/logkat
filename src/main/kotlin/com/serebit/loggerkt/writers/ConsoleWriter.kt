package com.serebit.loggerkt.writers

import com.serebit.loggerkt.LeveledLogMessage
import com.serebit.loggerkt.LogMessage

/**
 * An implementation of [LogWriter] that outputs to the console. This writer's output is color coded using ANSI
 * escape codes by default, unless the operating system is Windows.
 *
 * @constructor Creates a new instance, using ANSI color transforms if specified.
 * @param shouldUseAnsiColors Whether or not the writer should use ANSI color transforms. Defaults to true.
 */
class ConsoleWriter(shouldUseAnsiColors: Boolean = true) : LogWriter {
    private val useAnsiColors = if (System.getProperty("os.name").startsWith("Windows")) false else shouldUseAnsiColors

    /**
     * Prints the given [message] to the console, applying the proper ANSI color transform if [useAnsiColors] is set to
     * true and the given [message] is an instance of [LeveledLogMessage].
     */
    override fun log(message: LogMessage) = if (useAnsiColors && message is LeveledLogMessage) {
        println(message.withAnsiColorTransform)
    } else {
        println(message)
    }
}
