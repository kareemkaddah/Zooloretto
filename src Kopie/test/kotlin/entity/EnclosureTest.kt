package entity

import kotlin.test.*

/**
 * Tests an enclosure
 */
class EnclosureTest {
    /**
     * Enclosure with
     * one fertile male Zebra,
     * capacity of 5,
     * granting 2 coins to the player if full for the first time,
     * granting 8 points if full and 5 if almost full when the game ends.
     */
    private val enc1 = Enclosure(
        animals = mutableListOf(
            Card.Animal(AnimalType.Zebra, Maturity.Male, true)
        ),
        animalCapacity = 5,
        shopCapacity = 2,
        coinsFull = 1,
        pointsFull = 8,
        pointsNearlyFull = 5
    )

    /**
     * Enclosure with
     * two fertile male and
     * two non-fertile neutral Zebras,
     * capacity of 5,
     * granting 2 coins to the player if full for the first time,
     * granting 8 points if full and 5 if almost full when the game ends.
     */
    private val enc2 = Enclosure(
        animals = mutableListOf(
            Card.Animal(AnimalType.Zebra, Maturity.Male, true),
            Card.Animal(AnimalType.Zebra, Maturity.Male, true),
            Card.Animal(AnimalType.Zebra, Maturity.Neutral, false),
            Card.Animal(AnimalType.Zebra, Maturity.Neutral, false)
        ),
        animalCapacity = 5,
        shopCapacity = 2,
        coinsFull = 1,
        pointsFull = 8,
        pointsNearlyFull = 5
    )

    /**
     * Enclosure with a "family" (one female, one male and an offspring - now all non-fertile)
     * of Zebras and two non-fertile neutral Zebras,
     * capacity of 5,
     * granting 2 coins to the player if full for the first time,
     * granting 8 points if full and 5 if almost full when the game ends.
     */
    private val enc3 = Enclosure(
        animals = mutableListOf(
            Card.Animal(AnimalType.Zebra, Maturity.Male, false),
            Card.Animal(AnimalType.Zebra, Maturity.Female, false),
            Card.Animal(AnimalType.Zebra, Maturity.Neutral, false),
            Card.Animal(AnimalType.Zebra, Maturity.Neutral, false),
            Card.Animal(AnimalType.Zebra, Maturity.OffSpring, false)
        ),
        animalCapacity = 5,
        shopCapacity = 8,
        coinsFull = 1,
        pointsFull = 8,
        pointsNearlyFull = 5
    )

    /**
     * check if computePoints() works as intended
     */
    @Test
    fun computePoints(){
        /**
         * Enclosure with less than capacity-1 animals and no shops grants 0 points.
         * After adding a shop it grants amount of animals points.
         */
        assertEquals(0, enc1.computePoints())
        enc1.shops.add(0, Card.Shop(1))
        assertEquals(1, enc1.computePoints())
        /**
         * will be added in when offsprings are being correctly copulated
         */
        //  enc1.animals.add(Card.Animal(AnimalType.Zebra, Maturity.Female, true))
        //  assertEquals(3, enc1.computePoints())

        /**
         * Enclosure of capacity 4/5 gives pointsNearlyFull points,
         * after adding has capacity 5/5 and grants pointsFull points.
         */
        assertEquals(5, enc2.computePoints())
        enc2.animals.add(4, Card.Animal(AnimalType.Zebra, Maturity.Neutral, false))
        assertEquals(8, enc2.computePoints())

        /**
         * Enclosure of full capacity grants pointsFull points.
         * Adding a shop does nothing in this case.
         */
        assertEquals(8, enc3.computePoints())
        enc3.shops.add(0, Card.Shop(1))
        assertEquals(8, enc3.computePoints())

    }

    private val enc4 = Enclosure(
        animals = mutableListOf(
            Card.Animal(AnimalType.Zebra, Maturity.Male, false),
            Card.Animal(AnimalType.Zebra, Maturity.Female, false),
            null,
            null,
            null
        ),
        shops = mutableListOf(Card.Shop(1)),
        animalCapacity = 5,
        shopCapacity = 8,
        coinsFull = 1,
        pointsFull = 8,
        pointsNearlyFull = 5
    )

    /**
     * check if add() via addAll() works as intended
     * also check Card.toString()
     */
    @Test
    fun addAllTest(){
        val maleZebra = Card.Animal(AnimalType.Zebra, Maturity.Male, false)
        val s = "Animal(animalType=Zebra, maturity=Male, isFertile=false)"
        assertEquals(s, maleZebra.toString())
        val shop1 = Card.Shop(1)
        assertEquals("Shop(shopType=1)", shop1.toString())
        val coin = Card.Coin
        assertEquals("Coin", coin.toString())
        val elements = listOf(maleZebra, shop1)
        assertEquals(2, enc4.animals.filterNotNull().size )
        assertEquals(1, enc4.shops.filterNotNull().size )
        enc4.addAll(elements)
        assertEquals(3, enc4.animals.filterNotNull().size )
        assertTrue(enc4.animals.contains(maleZebra))
        assertEquals(2, enc4.shops.filterNotNull().size )
        assertTrue(enc4.shops.contains(shop1))
        assertFails {  enc4.add(coin) }

    }
}