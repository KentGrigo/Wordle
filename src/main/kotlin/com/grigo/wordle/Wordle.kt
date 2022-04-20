package com.grigo.wordle

import io.github.resilience4j.core.StopWatch
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

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

fun occurringLetters(alphabet: Iterable<Char>, words: Collection<String>): HashMap<Char, Int> {
    val letterToOccurrences = HashMap<Char, Int>()
    for (letter in alphabet) {
        letterToOccurrences[letter] = 0
    }
    for (word in words) {
        for (letter in word.toSet()) {
            letterToOccurrences[letter] = 1 + letterToOccurrences[letter]!!
        }
    }
    return letterToOccurrences
}

fun mostRepresentingWord(words: Collection<String>, letterToOccurrences: Map<Char, Int>): String {
    var bestWord = words.first()
    var bestScore = 0
    for (word in words) {
        var score = 0
        for (letter in word.toSet()) {
            score += letterToOccurrences[letter]!!
        }
        if (bestScore < score) {
            bestWord = word
            bestScore = score
        }
    }
    return bestWord
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

    var modifiableDictionary = language.dictionary.filter { it.length == wordLength }.toSet()
    val letters = language.alphabet.toMutableList()
    val includingLetters = HashSet<Char>()
    val excludingLetters = HashSet<Char>()
    val positionToLetter = HashMap<Int, Char>()
    val positionToLetters = HashMap<Int, ArrayList<Char>>()
    for (index in 0 until wordLength) {
        val positionedLetters = ArrayList<Char>()
        positionedLetters.addAll(letters)
        positionToLetters[index] = positionedLetters
    }

    val targetWord = givenTargetWord ?: pickTargetWord(playStyle, modifiableDictionary)
    if (isLogging) {
        if (targetWord == null) {
            println("Target word is unknown")
        } else {
            println("Target word is $targetWord")
        }
        println()
    }

    var tries = 0
    while (true) {
        tries += 1

        if (isLogging) {
            println("Tries: $tries")
            println("Target: $targetWord")
            println(letters.sorted())
            println(includingLetters.toMutableList().sorted())
            println(excludingLetters.toMutableList().sorted())
            println(positionToLetter)
            for ((index, chars) in positionToLetters) {
                println("$index: ${chars.sorted()}")
            }
        }

        if (1 < tries) {
            modifiableDictionary = modifiableDictionary.filter { word ->
                positionToLetter.all { (position, letter) -> letter == word[position] } &&
                        word.indices.all { index -> word[index] in positionToLetters[index]!! } &&
                        includingLetters.all { letter -> letter in word } &&
                        excludingLetters.all { letter -> letter !in word }
            }.toSet()
        }

        val suggestion = when (playStyle) {
            PlayStyle.PLAY_LOCAL, PlayStyle.PLAY_EXTERNAL -> {
                var suggestion: String
                while (true) {
                    print("Suggestion> ")
                    suggestion = scanner.next()
                    if (suggestion in language.dictionary) {
                        break
                    }
                }
                println()
                suggestion
            }
            PlayStyle.SOLVE_LOCAL, PlayStyle.SOLVE_EXTERNAL -> {
                val letterToOccurrences = occurringLetters(language.alphabet, modifiableDictionary)
                val suggestion = mostRepresentingWord(modifiableDictionary, letterToOccurrences)
                if (isLogging) {
                    println("Suggestion: $suggestion")
                    println()
                }
                suggestion
            }
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

        val badLetters = HashSet<Char>()
        val goodLetters = HashSet<Char>()
        for ((letter, color) in suggestion.zip(colors)) {
            if (color == 'G') {
                badLetters.add(letter)
            } else {
                goodLetters.add(letter)
            }
        }
        letters.removeAll(badLetters)
        includingLetters.addAll(goodLetters)
        excludingLetters.addAll(badLetters)
        for (index in 0 until wordLength) {
            positionToLetters[index]!!.removeAll(badLetters)
        }
        for ((index, letterAndColor) in suggestion.zip(colors).withIndex()) {
            val (letter, color) = letterAndColor
            if (color == 'E') {
                positionToLetters[index] = arrayListOf(letter)
            } else {
                positionToLetters[index]!!.remove(letter)
            }
        }

        for (index in 0 until wordLength) {
            val color = colors[index]
            val letter = suggestion[index]
            if (color == 'E') {
                positionToLetter[index] = letter
            }
        }

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
