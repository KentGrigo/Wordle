package com.grigo.wordle

sealed class Language(
    val alphabet: Collection<Char>,
    val dictionary: Collection<String>,
) {
    constructor(dictionary: Collection<String>) : this(alphabet(dictionary), dictionary)

    companion object {
        private fun alphabet(dictionary: Collection<String>): Set<Char> =
            dictionary.toSet().map { it.toSet() }.flatten().toSet()

        private fun dictionary(fileName: String): List<String> {
            val dictionary = resourceAsText("/dictionaries/$fileName")
            return dictionary
                .filter { it == it.lowercase() }
        }

        private fun resourceAsText(path: String): List<String> =
            object {}.javaClass
                .getResource(path)
                ?.readText(Charsets.ISO_8859_1)
                ?.split("\n")
                ?: throw Exception("The resource at $path was not found")

        fun danishDictionary(): List<String> =
            dictionary("DanishDictionary.txt")
                .asSequence()
                .filter { word -> 'é' !in word }
                .filter { word -> 'í' !in word }
                .filter { word -> 'ó' !in word }
                .filter { word -> 'ö' !in word }
                .filter { word -> 'ü' !in word }
                .toList()

        fun englishDictionary(): List<String> =
            dictionary("EnglishDictionary.txt")
    }
}

class Danish : Language(danishDictionary())
class English : Language(englishDictionary())
