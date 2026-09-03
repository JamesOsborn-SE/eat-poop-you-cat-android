package dev.develsinthedetails.eatpoopyoucat.core.utilities

fun validateNickname(
    currentNickname: String?,
    takenNicknames: List<String>,
): Boolean = !(currentNickname.isNullOrBlank() || takenNicknames.contains(currentNickname))

fun generateNickname(
    hardcodedNames: List<String>,
    takenNicknames: List<String>,
    fallbackName: String
): String = hardcodedNames
    .filterNot { takenNicknames.contains(it) }
    .randomOrNull() ?: fallbackName