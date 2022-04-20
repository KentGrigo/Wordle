package com.grigo.wordle

class Knowledge(
    private val wordLength: Int,
    var modifiableDictionary: Collection<String>,
    private val letters: MutableCollection<Char>,
    private val includingLetters: MutableCollection<Char>,
    private val excludingLetters: MutableCollection<Char>,
    private val positionToLetter: HashMap<Int, Char>,
    private val positionToLetters: HashMap<Int, ArrayList<Char>>,
) {
    constructor(
        wordLength: Int,
        language: Language,
    ) : this(
        wordLength,
        language.dictionary.filter { it.length == wordLength }.toSet(),
        language.alphabet.toMutableList(),
    )

    constructor(
        wordLength: Int,
        modifiableDictionary: Collection<String>,
        letters: MutableCollection<Char>,
    ) : this(
        wordLength,
        modifiableDictionary,
        letters,
        HashSet(),
        HashSet(),
        HashMap(),
        computePositionToLetters(letters, wordLength),
    )

    fun update(suggestion: String, colors: String) {
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

        modifiableDictionary = modifiableDictionary.filter { word ->
            positionToLetter.all { (position, letter) -> letter == word[position] } &&
                    word.indices.all { index -> word[index] in positionToLetters[index]!! } &&
                    includingLetters.all { letter -> letter in word } &&
                    excludingLetters.all { letter -> letter !in word }
        }.toSet()
    }

    fun print() {
        println(letters.sorted())
        println(includingLetters.toMutableList().sorted())
        println(excludingLetters.toMutableList().sorted())
        println(positionToLetter)
        for ((index, chars) in positionToLetters) {
            println("$index: ${chars.sorted()}")
        }
        println("Possibilities: ${modifiableDictionary.size}")
        if (modifiableDictionary.size < 10) {
            println(modifiableDictionary)
        }
    }
}

fun computePositionToLetters(
    letters: Collection<Char>, wordLength: Int,
): HashMap<Int, ArrayList<Char>> {
    val positionToLetters = HashMap<Int, ArrayList<Char>>()
    for (index in 0 until wordLength) {
        val positionedLetters = ArrayList<Char>()
        positionedLetters.addAll(letters)
        positionToLetters[index] = positionedLetters
    }
    return positionToLetters
}
