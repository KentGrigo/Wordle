# Wordle

## What is this?

This repository contains my implementation of and solver to [Wordle](https://www.nytimes.com/games/wordle/index.html).

Wordle is a word-guessing game made by Josh Wardle, and bought and published by The New York Times Company. You get six
guess to guess a five-letter word. For each guess, you get further clues on what the right word is based on the used
letters and their placement.

- If the letter is not in the word, it will be white :white_large_square:.
- If the letter is in the word but at a wrong place, it will be yellow :yellow_square:.
- If the letter is in the word and at the right place, it will be green :green_square:.

There might be duplicate letters, both in your guess and in the right word. In that case, the letters in your guess will
get as many colors as there are in the right word, focusing on letters with the correct placement and then from left to
right.
