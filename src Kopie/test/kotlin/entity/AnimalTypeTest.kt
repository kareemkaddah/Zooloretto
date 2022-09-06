package entity

import kotlin.test.*

/**
 * Tests the AnimalType enum
 */
class AnimalTypeTest {

    /**
     * Tests the tostring method
     */
    @Test
    fun testToString(){
        for(a in AnimalType.values()){
            assertEquals(a.name,a.toString())
        }
    }
}