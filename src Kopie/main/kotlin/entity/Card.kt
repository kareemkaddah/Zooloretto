package entity

import java.io.Serializable

/**
 * This class represents one playing card, which can of the type animal, shop or coin.
 */
sealed class Card : Serializable {
    val  serialVersionUID: Long = 6585098290L
    /**
     * Animal Card
     */
    data class Animal(
        var animalType: AnimalType = AnimalType.Panda,
        var maturity: Maturity = Maturity.Neutral,
        var isFertile: Boolean = false,
    ) : Serializable, Card()

    /**
     * Shop Card
     */
    data class Shop(var shopType: Int) : Serializable, Card()

    /**
     * Coin Card
     */
    object Coin : Serializable, Card()

    /**
     * method to print a Card
     */
    override fun toString(): String = when(this){
            is Animal -> "Animal,"+this.animalType+","+this.maturity
            is Shop -> "Shop,"+this.shopType
            is Coin -> "Coin"
        }

}
