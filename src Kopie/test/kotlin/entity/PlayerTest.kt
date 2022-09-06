package entity

import kotlin.test.*

/**
 * Tests [Player]
 */
class PlayerTest {

    /**
    * Tests the tostring method
    */
    @Test
    fun testToString(){
        val a = Player.HumanPlayer("CoolerName")
        assertEquals("CoolerName",a.toString())
        val b = Player.AIPlayer("Mr.Roboto")
        assertEquals("Mr.Roboto",b.toString())
    }

    /**
     * Tests the tostring method
     */
    @Test
    fun scoreTest(){
        val a = Player.HumanPlayer("CoolerName")
        a.zoo = Zoo(2)
        assertEquals(0,a.score())
    }



}