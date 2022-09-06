package entity

import java.io.File
import java.io.FileInputStream


/**
 * The Game
 *
 * @param players the list of all playeres
 * @param currentPlayer the currentPlayer
 * @param simulationSpeed the speed the KI works with
 * @param loadFromFile is the file that contains the cards preset.
 */
data class ZoolorettoGame(
    var players: List<Player>,
    var currentPlayer: Player = players[0],
    val simulationSpeed: Int = 1000,
    val loadFromFile : File? = null,
    var playedTurns : MutableList<String> = mutableListOf()
) : java.io.Serializable {


    /**
     * game scores
     * List of every player with the Score of that Player
     */

    var gameScore: ArrayList<Pair<Player,Int>> = ArrayList<Pair<Player,Int>>()




    /**

     *
     * Used for draw action.
     */
    var drawnCard: Card? = null
    var bankCoins = 30 - players.size * 2
    var deck: MutableList<Card> = mutableListOf()
    var extraCards: MutableList<Card> = mutableListOf()

    /**
     * The constructor, which creates a new and correct game and deck
     */
    init {
        if(loadFromFile == null){
            players.forEach{it.zoo = Zoo(players.size)}
            var animalTypes = AnimalType.values().toMutableList()
            animalTypes.shuffle()
            animalTypes = animalTypes.take(
                when (players.size) {
                    2 -> 5
                    3 -> 6
                    4 -> 7
                    else -> 8
                }
            ).toMutableList()


            val animalCards = animalTypes.map {animalType ->
                List(2) { Card.Animal(animalType,maturity = Maturity.Male, isFertile = true) } +
                        List(2) { Card.Animal(animalType,maturity = Maturity.Female, isFertile = true) } +
                        List(7) { Card.Animal(animalType,maturity = Maturity.Neutral) }
            }.flatten()

            val shops = List(12){Card.Shop(it%3)}
            val coins = List(12){Card.Coin}

            deck = (animalCards+shops+coins).toMutableList()
            deck.shuffle()
        }else{
            val inputAsList = FileInputStream(loadFromFile).bufferedReader().use { it.readLines() }.toMutableList()
            inputAsList.removeFirst() // Remove first becaues first lien is the player amount
            for(cardAsString in inputAsList) {
                when (cardAsString[0]) {
                    'C' -> deck.add(Card.Coin)
                    'V' -> deck.add(Card.Shop(shopType = cardAsString[1].code))
                    else -> {
                        deck.add(
                            Card.Animal(
                                animalType = when (cardAsString[0]) {
                                    'F' -> AnimalType.Flamingo
                                    'K' -> AnimalType.Kamel
                                    'L' -> AnimalType.Leopard
                                    'E' -> AnimalType.Elefant
                                    'P' -> AnimalType.Panda
                                    'S' -> AnimalType.Schimpanse
                                    'Z' -> AnimalType.Zebra
                                    else -> AnimalType.Kaenguru
                                },
                                maturity = when (cardAsString[1]) {
                                    'm' -> Maturity.Male
                                    'w' -> Maturity.Female
                                    else -> Maturity.Neutral
                                },
                                isFertile = ('-' != cardAsString[1])
                            )
                        )
                    }
                }
            }
        }
        extraCards = MutableList(15){deck.removeLast()}
        extraCards = extraCards.reversed().toMutableList()
    }

    var trucks: MutableList<Truck> = when (players.size) {
        2 -> mutableListOf(Truck(), Truck(capacity = 2), Truck(capacity = 1))
        else -> MutableList(players.size){Truck()}
    }

    /**
     * function that returns a sorted list of scores. to be called at the end of the game.
     */
    fun calculateHighScores(){
        for( player in players){
            gameScore.add(Pair(player,player.score()))
        }
        gameScore.sortBy { t->-t.second }
    }

}