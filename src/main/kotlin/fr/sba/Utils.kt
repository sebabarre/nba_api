package fr.sba

import java.io.BufferedReader
import java.io.InputStream

fun fileToString(inputStream: InputStream): String {
    val reader = BufferedReader(inputStream.reader())
    return reader.use {
        it.readText()
    }
}