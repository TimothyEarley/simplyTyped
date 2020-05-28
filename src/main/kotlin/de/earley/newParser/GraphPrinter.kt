package de.earley.newParser

import java.io.FileWriter

fun Parser<*, *>.graphWriter(dest : String) {
    FileWriter(dest).use {
        it.write("""
            digraph { ${toDot(hashSetOf())} }
        """.trimIndent())
    }
}

fun Parser<*, *>.dotNode(label : String) = "${dotName()} [label=\"$label\"];\n"
fun dotPath(from : Parser<*, *>, to : Parser<*, *>, comment: String = "")
        = "${from.dotName()} -> ${to.dotName()} [label=\"$comment\"];\n"