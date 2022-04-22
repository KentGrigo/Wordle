package com.grigo.wordle

import java.util.*

data class Player(
    private val language: Language,
    private val scanner: Scanner = Scanner(System.`in`, Charsets.ISO_8859_1.name()),
) {
    fun getSuggestion(): String {
        var suggestion: String
        while (true) {
            print("Suggestion> ")
            suggestion = scanner.next()
            if (suggestion in language.dictionary) {
                break
            }
        }
        println()
        return suggestion
    }
}
