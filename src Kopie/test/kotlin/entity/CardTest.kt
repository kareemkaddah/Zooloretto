package entity

import kotlin.test.*

/**
 * Tests [Card]
 */
class CardTest {

    /**
     * Tests the tostring method
     */
    @Test
    fun testToString(){
        val a = Card.Coin
        assertEquals("Coin",a.toString())
        val b = Card.Shop(7)
        assertEquals("Shop(shopType=7)",b.toString())
        val c = Card.Animal(animalType = AnimalType.Elefant,Maturity.Neutral,true)
        assertEquals("Animal(animalType=Elefant, maturity=Neutral, isFertile=true)",c.toString())

        /*
        val t ="Animal(animalType=Elefant, maturity=Neutral, isFertile=true)"
        val ar = t.split("Animal(animalType=",", maturity=", "isFertile=", ",", " ", ")", ignoreCase = true)
        val sss = t.substringAfter("=")
        println( ar)
        println( ar[1])
        println( ar[2])
        println( ar[5])
         */
    }
}