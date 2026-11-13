package com.example.myaplication.logic
enum class Difficulty(val label: String, val cellsToRemove: Int, val baseScore: Int) {
    FACIL("Fácil", 36, 2000),
    MEDIO("Médio", 46, 3000),
    DIFICIL("Difícil", 52, 4000);
    companion object {
        fun fromName(name: String?): Difficulty =
            entries.firstOrNull { it.name == name } ?: MEDIO
        fun fromApi(apiValue: String?): Difficulty = when
                (apiValue?.lowercase()) {
            "easy" -> FACIL
            "hard" -> DIFICIL
            else -> MEDIO
        }
    }
}