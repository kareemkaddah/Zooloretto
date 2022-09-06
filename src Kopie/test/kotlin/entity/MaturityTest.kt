package entity

import kotlin.test.*

/**
 * Tests [Maturity]
 */
class MaturityTest {

    /**
     * Tests the tostring method
     */
    @Test
    fun testToString(){
        for(a in Maturity.values()){
            assertEquals(a.name,a.toString())
        }
    }
}