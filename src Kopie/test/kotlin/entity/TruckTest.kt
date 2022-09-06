package entity

import kotlin.test.*

/**
 * Tests [Truck]
 */
class TruckTest {

    /**
     * Tests the add Method
     */
    @Test
    fun addTest(){
        val a = Truck()
        a.add(Card.Coin)
        assertEquals(3,a.cards.size)
        assertEquals(1,a.cards.filterNotNull().size)
        assertEquals(Card.Coin,a.cards[0])
    }

    /**
     * Tests the addAll Method
     */
    @Test
    fun addAllTest(){
        val a = Truck()
        a.addAll(listOf(Card.Coin,Card.Shop(7)))
        assertEquals(3,a.cards.size)
        assertEquals(2,a.cards.filterNotNull().size)
        assertEquals(Card.Coin,a.cards[0])
        assertEquals(Card.Shop(7),a.cards[1])
    }








}