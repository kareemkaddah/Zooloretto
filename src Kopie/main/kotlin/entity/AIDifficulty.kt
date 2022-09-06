package entity

/**
 * Enum representing the three levels of AI difficulty. Which one can you beat?
 * Write down in the comments!
 */
enum class AIDifficulty {
    EASY,
    MEDIUM,
    HARD,
    ;

    /**
     * Reads the difficulty level.
     */
    override fun toString() = when(this) {
        EASY -> "Easy"
        MEDIUM -> "Medium"
        HARD -> "Hard"
    }
    /**
    returns the difficulty as an int
     */
    fun toInt() =when(this) {
        EASY -> 1
        MEDIUM -> 2
        HARD -> 3
    }
}
