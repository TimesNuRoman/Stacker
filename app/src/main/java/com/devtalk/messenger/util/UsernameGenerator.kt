package com.devtalk.messenger.util

import kotlin.random.Random

object UsernameGenerator {

    private val prefixes = listOf(
        "shadow", "cyber", "ghost", "null", "void", "dark",
        "neo", "glitch", "byte", "hack", "root", "zero",
        "phantom", "crypt", "nexus", "pulse", "flux", "storm",
        "binary", "vortex", "stealth", "onyx", "raven", "oxide",
        "static", "proxy", "daemon", "kernel", "vector", "chaos",
        "delta", "omega", "sigma", "matrix", "spider", "cobra",
        "wraith", "spectre", "cipher", "enigma", "cortex", "nova",
        "prism", "helix", "photon", "quark", "razor", "rift"
    )

    private val suffixes = listOf(
        "dev", "ops", "sec", "net", "sys", "hex",
        "bit", "node", "link", "code", "run", "exec",
        "log", "key", "io", "fx", "rx", "tx",
        "x", "z", "v2", "3d", "ai", "ml",
        "42", "01", "404", "x86", "arm", "ssh"
    )

    private val separators = listOf("_", "-", "")

    fun generate(): String {
        val prefix = prefixes.random()
        val suffix = suffixes.random()
        val sep = separators.random()
        return "$prefix$sep$suffix"
    }

    fun generateBatch(count: Int = 6): List<String> {
        val results = mutableSetOf<String>()
        while (results.size < count) {
            results.add(generate())
        }
        return results.toList()
    }

    fun generateWithNumber(): String {
        val prefix = prefixes.random()
        val num = Random.nextInt(10, 999)
        return "${prefix}_$num"
    }

    fun generateShort(): String {
        val prefix = prefixes.random()
        return if (Random.nextBoolean()) {
            "${prefix}${Random.nextInt(1, 99)}"
        } else {
            prefix
        }
    }
}
