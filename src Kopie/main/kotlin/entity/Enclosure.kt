package entity

/**
 * Represents a zoo enclosure and the barn
 *
 * @param animals the animals of the enclosure
 * @param shops the shops of the enclosure
 * @param isBarn is trur if this is the barn
 * @param animalCapacity the maximum amount of animals that can be stored
 * @param shopCapacity the maximum amount of shops that can be stored
 * @param coinsFull the coins the player gets when the enclosure is full
 * @param pointsFull the points the player gets when the enclosure is full
 * @param pointsNearlyFull the points the player gets when the enclosure is nearlyfull
 */
class Enclosure (
    var isBarn: Boolean = false,
    val animalCapacity: Int,
    val shopCapacity: Int,
    var animals: MutableList<Card.Animal?> = MutableList(animalCapacity){null},
    var shops: MutableList<Card.Shop?> = MutableList(shopCapacity){null},
    var coinsFull: Int,
    val pointsFull: Int,
    val pointsNearlyFull: Int
) : java.io.Serializable {

    /**
     * Computes the points whis enclosure is worth
     */
    fun computePoints() = when (animals.filterNotNull().size) {
        animalCapacity - 1 -> pointsNearlyFull
        animalCapacity -> pointsFull
        else -> if (shops.filterNotNull().isNotEmpty()) animals.size else 0
    }

    /**
     * Adds a Card to the enclosure
     */
    fun add(card : Card){
        when(card){
            is Card.Animal -> {
                animals[animals.indexOfFirst { it==null }] = card
            }
            is Card.Shop -> {
                require(shops.filterNotNull().size<=shopCapacity)
                if (shops.filterNotNull().size!=shops.size){
                    shops[shops.indexOf(shops.filter{it==null}[0])] = card
                }else {
                shops.add(card)
                }
            }
            else -> throw IllegalStateException("Cannot add this type of card to an Enclosure")
        }
    }

    /**
     * Adds multiple cards to animals or shops
     */
    fun addAll(elements: List<Card>) = elements.forEach { add(it) }

    /**
     * method to print an Enclosure
     */
    override fun toString(): String = "Enclosure($animals,$shops)"
}