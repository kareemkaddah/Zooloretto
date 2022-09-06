package entity

import kotlin.test.*

/**
 * Tests the AnimalType enum
 */
class ZooTest {

    /**
     * Test the Zoo Enclosures
     */
    @Test
    fun enclosureTest(){
        val a = Zoo(2)
        assertTrue(a.enclosures[0].isBarn)
        assertEquals(4,a.enclosures.size)
    }

    /**
     * Test the Zoo expand method
     */
    @Test
    fun expandTest(){
        var a = Zoo(2)
        assertEquals(2,a.expanded)
        a.expand()
        assertEquals(1,a.expanded)
        a.expand()
        assertEquals(0,a.expanded)

        a = Zoo(3)
        assertEquals(1,a.expanded)
        a.expand()
        assertEquals(0,a.expanded)

    }

}