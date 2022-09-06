package entity

/**
 * Maturity of an animal.
 */
enum class Maturity {
    Female,
    Male,
    Neutral,
    OffSpring,
    ;

    /**
     * Provides a way to print out Maturity.
     */
    override fun toString() = this.name
}
