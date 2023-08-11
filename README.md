# [StupidStackLanguage](https://esolangs.org/wiki/StupidStackLanguage) Tables
Code to generate a JSON file which contains the shortest piece of *StupidStackLanguage* code to change the top of the stack.
Outputs JSON in the format `{ [start: number]: { [end: number]: string } }`

## Wat?
StupidStackLanguage is an esoteric programming language which I created in which all operations take place on a [stack](https://en.wikipedia.org/wiki/Stack_(abstract_data_type)). It has 26 instructions, each represented by one character of `A-Z` (case insensitive). This repository includes code to *generate* StupidStackLanguage programs which, when executed, will modify the stack so that a different specific value is at the top. This code always attempts to output the shortest programs to get the stack from a state of `top=X` to `top=Y`.

## Why?
The output of this will be used to improve the code generation functionality of my online StupidStackLanguage interpreter.

## Optimizations
1. Programs with the `B` (pop) instruction immediately following a stack-pushing instruction will not be searched.
2. If an unfinished program causes an error while searching, no further variations of that program will be searched, since it is known that they will all cause errors too.