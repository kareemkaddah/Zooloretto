package entity

/**
 * This class represents an abstract Zooloretto player. Player can be either a [HumanPlayer] or an advanced [AIPlayer].
 * @param playerName the name of the player. Default "Superclass Player"
 */
sealed class Player(private val playerName: String = "Superclass Player") : java.io.Serializable {
    var balance = 2
    lateinit var zoo: Zoo
    var selectedTruck: Truck? = null
    var highscore = 0
    val  serialVersionUID: Long = 6529685098267757690L
    var hasGottenHint= false


    /**
     * returns the player name
     */
   fun  getPlayerName():String{
        return this.playerName
    }

    /**
     * AI player's class.
     * @param name is the player's name. Default is "Bot".
     * @param difficulty is the AI's difficulty level. Default is [AIDifficulty.EASY].
     * See also [AIDifficulty].
     * @param speed is the simulation speed / the delay in milliseconds before AI makes a move.
     * Default is 1000.
     */
    data class AIPlayer(
        val name: String = "Bot",
        val difficulty: AIDifficulty = AIDifficulty.EASY,
        val speed: Number = 1000
    ) : Player(name) {
        /**
         * Reads the player's name.
         */
        override fun toString(): String {
            return name
        }
    }

    /**
     * Human player's class.
     * @param name is the player's name. Default is "Player".
     */
    data class HumanPlayer(val name: String = "Player") : Player(name) {
        /**
         * Reads the player's name.
         */
        override fun toString(): String {
            return name
        }
    }

    /**
     * Calculates the score based on the current state of the board.
     * @return the score from each enclosure in addition to the number of the stalls.
     */
    fun score(): Int {
        return (
            zoo.enclosures.filter { !it.isBarn }.sumOf { it.computePoints() }
                    + (zoo.enclosures.filter { !it.isBarn }.map { it.shops }.flatten()
                .filterNotNull().groupBy { it.shopType }.size * 2)
                    - (zoo.barn.shops.filterNotNull().size * 2)
                    - (zoo.barn.animals.filterNotNull().groupBy { it.animalType }.size * 2)
        )
    }
}