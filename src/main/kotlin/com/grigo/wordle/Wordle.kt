package com.grigo.wordle

import io.github.resilience4j.core.StopWatch
import java.util.*

fun main() {
    val wordLength = 5
    val language = Danish()

    val stopWatch = StopWatch.start()
    for (testNumber in 0 until 1000) {
        wordleSolver(language, wordLength, PlayStyle.SOLVE_LOCAL, false, null)
    }
    val totalTime = stopWatch.stop()
    println("Total time: $totalTime")
}

enum class PlayStyle {
    PLAY_LOCAL,
    SOLVE_LOCAL,
    PLAY_EXTERNAL,
    SOLVE_EXTERNAL,
}

fun pickTargetWord(playStyle: PlayStyle, dictionary: Collection<String>): String? {
    return when (playStyle) {
        PlayStyle.PLAY_LOCAL -> dictionary.random()
        PlayStyle.SOLVE_LOCAL -> dictionary.random()
        PlayStyle.SOLVE_EXTERNAL -> null
        PlayStyle.PLAY_EXTERNAL -> null
    }
}

fun wordleSolver(
    language: Language,
    wordLength: Int,
    playStyle: PlayStyle,
    isLogging: Boolean = false,
    givenTargetWord: String? = null,
): Int {
    val stopWatch = StopWatch.start()
    val scanner = Scanner(System.`in`, Charsets.ISO_8859_1.name())
    val player = Player(language, scanner)

    val knowledge = Knowledge(wordLength, language)
    val solver = Solver(language, knowledge)

    val targetWord = givenTargetWord ?: pickTargetWord(playStyle, knowledge.modifiableDictionary)
    if (isLogging) {
        if (targetWord == null) {
            println("Target word is unknown")
        } else {
            println("Target word is '$targetWord'")
        }
        println()
    }

    var tries = 0
    while (true) {
        tries += 1

        if (isLogging) {
            println("Tries: $tries")
            println("Target: $targetWord")
            knowledge.print()
        }

        val suggestion = when (playStyle) {
            PlayStyle.PLAY_LOCAL, PlayStyle.PLAY_EXTERNAL -> {
                player.getSuggestion()
            }
            PlayStyle.SOLVE_LOCAL, PlayStyle.SOLVE_EXTERNAL -> {
                solver.getSuggestion()
            }
        }
        if (isLogging) {
            println("Suggestion: $suggestion")
            println()
        }

        val colors = if (targetWord == null) {
            print("Colors> ")
            scanner.next()
        } else {
            var colors = ""
            for ((suggestionLetter, targetLetter) in suggestion.zip(targetWord)) {
                colors += if (suggestionLetter == targetLetter) {
                    'E'
                } else if (suggestionLetter in targetWord) { // TODO: Check duplicates
                    'Y'
                } else {
                    'G'
                }
            }
            if (playStyle == PlayStyle.PLAY_LOCAL) {
                println("Colors: $colors")
                println()
            }
            colors
        }

        knowledge.update(suggestion, colors)

        val status = if (colors.all { it == 'E' }) {
            "SUCCESS"
        } else {
            null
        }
        if (status != null) {
            val timing = stopWatch.stop()
            println("$status: $targetWord, tries: $tries, time taken: $timing")
            break
        }
    }
    return tries
}
