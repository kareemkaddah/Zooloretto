package entity

/**
 * The zoo resembles the entire board of a player's.
 *
 * @param playersCount is the number of players in the current game.
 */
class Zoo(playersCount: Int) : java.io.Serializable {
    /**
     * Initialize all 3 enclosures.
     */
    val enclosures = mutableListOf(
        Enclosure(
            animalCapacity = 100,
            shopCapacity = 100,
            coinsFull = 0,
            pointsFull = 0,
            pointsNearlyFull = 0,
            isBarn = true
        ),
        Enclosure(animalCapacity = 6, shopCapacity = 1, coinsFull = 0, pointsFull = 10, pointsNearlyFull = 6),
        Enclosure(animalCapacity = 5, shopCapacity = 1, coinsFull = 2, pointsFull = 8, pointsNearlyFull = 5),
        Enclosure(animalCapacity = 4, shopCapacity = 2, coinsFull = 1, pointsFull = 5, pointsNearlyFull = 4)
    )

    /**
     * the Barn
     */
    var barn = enclosures[0]

    /**
     * Refers the number of remaining possible expansions according to the game rules.
     */
    var expanded = when (playersCount) {
        2 -> 2
        else -> 1
    }

    /**
     * Performs expand action, if possible, by adding a new enclosure to the zoo.
     */
    fun expand() {
        if (expanded > 0) {
            enclosures.add(enclosures.size,
                Enclosure(
                    isBarn = false,
                    animalCapacity = 6,
                    shopCapacity = 1,
                    coinsFull = 1,
                    pointsFull = 9,
                    pointsNearlyFull = 5
                )
            )
            expanded--
        }
    }


}