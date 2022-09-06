package entity

/**
 * Different types of animals.
 */
enum class AnimalType {
    Flamingo,
    Kamel,
    Leopard,
    Elefant,
    Panda,
    Schimpanse,
    Zebra,
    Kaenguru,
    ;

    /**
     * Provides a way to print out AnimalType.
     */
    override fun toString() = this.name
}
