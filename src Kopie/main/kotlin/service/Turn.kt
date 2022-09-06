package service

import entity.*

/**
 * This class represents a turn
 */
sealed class Turn {
    /**
     * Draw a card and place it on a truck
     */
    data class Draw(var truck : Truck, var index : Int) : Turn(){
        /**
         * ToString methode von Draw
         */
        override fun toString() : String = "eine Karte auf einen Truck gelegt."
    }

    /**
     * Moneyaction
     */
    data class MoneyAction(val m : service.MoneyAction) : Turn(){
        /**
         * ToString methode von Turn Moneyaction
         */
        override fun toString() : String = m.toString()
    }

    /**
     * Take a truck
     */
    data class TakeTruck(var truck : Truck,var map : List<Triple<Card,Enclosure?,Int>>) : Turn(){
        /**
         * ToString methode von TakeTruck
         */
        override fun toString() : String = "einen truck aus der Mitte genommen."
    }

    /**
     * Turn toString methode
     *
     * @return String welcher beschreibt was dieser Turn macht
     */
    override fun toString() : String = ""
}
