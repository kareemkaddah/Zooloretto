package service

import entity.AnimalType
import entity.Card
import entity.Enclosure
import entity.Player

/**
 * This class represents a specific MoneyAction
 */
sealed class MoneyAction {
    /**
     * Move1Token Moneyaction
     */
    data class Move1Token(val moveLocation : Enclosure, val chosenCard : Card,
                          val index : Int,val takeFromLocation:Enclosure?=null) : MoneyAction()

    /**
     * Swap2Types Moneyaction
     */
    data class Swap2Types(val map : Map<Enclosure,AnimalType>) : MoneyAction()

    /**
     * Purchase1Token Moneyaction
     */
    data class Purchase1Token(
        val moveLocation: Enclosure,
        val sPlayer: Player,
        val chosenAnimal: Card,
        val index: Int
    ) : MoneyAction()

    /**
     * Discard1Token Moneyaction
     */
    data class Discard1Token(val chosenCard : Card) : MoneyAction()

    /**
     * ExpandZoo Moneyaction
     */
    object ExpandZoo : MoneyAction(){
        /**
         * ToString methode von ExpandZoo
         */
        override fun toString() : String = "seinen Zoo erweitert."
    }
    object Skip :MoneyAction()
    /**
     * MoneyAction toString methode
     *
     * @return String welcher beschreibt was diese MoneyAction macht
     */
    override fun toString() : String = ""
}
