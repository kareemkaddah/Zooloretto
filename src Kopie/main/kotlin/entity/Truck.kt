package entity

/**
 * Zooloretto Truck!
 *
 * @param cards the cards this truck has
 * @param capacity the maximum amount of cards this truck can have
 */
class Truck(
    var cards: MutableList<Card?> = MutableList(3) { null },
    val capacity: Int = 3,
    var aiConsider: Boolean = true
) : java.io.Serializable {
    /**
     * Adds a Card to the enclosure
     */
    fun add(card : Card) {
        cards[cards.indexOf(cards.filter{it==null}[0])] = card
    }

    /**
     * Adds multiple cards to animals or shops
     */
    fun addAll(elements: Collection<Card>) = elements.forEach { add(it) }
}