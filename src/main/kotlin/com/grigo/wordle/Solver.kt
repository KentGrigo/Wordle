package com.grigo.wordle

data class Solver(
    private val language: Language,
    private val knowledge: Knowledge,
) {
    fun getSuggestion(): String {
        val letterToOccurrences = occurringLetters(language.alphabet, knowledge.modifiableDictionary)
        return mostRepresentingWord(knowledge.modifiableDictionary, letterToOccurrences)
    }

    private fun occurringLetters(alphabet: Iterable<Char>, words: Collection<String>): HashMap<Char, Int> {
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

    private fun mostRepresentingWord(words: Collection<String>, letterToOccurrences: Map<Char, Int>): String {
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
}
