package entity

import kotlin.test.*

/**
 * Tests [Truck]
 */
class ZoolorettoGameTest {

    /**
     * Tests the init
     */
    @Test
    fun initTest(){
        val a = ZoolorettoGame(listOf(Player.HumanPlayer("a"),Player.HumanPlayer("b")))
        //assertEquals((112-15),a.deck.size)
        assertEquals(15,a.extraCards.size)
        assertEquals(3,a.trucks.size)
    }


}