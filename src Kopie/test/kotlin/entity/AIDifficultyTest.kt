package entity

import kotlin.test.*

/**
 * Tests the AIDifficulty enum
 */
class AIDifficultyTest {

    /**
     * Tests the tostring method
     */
    @Test
    fun testToString(){
        val a = AIDifficulty.EASY
        assertEquals("Easy",a.toString())
        val b = AIDifficulty.MEDIUM
        assertEquals("Medium",b.toString())
        val c = AIDifficulty.HARD
        assertEquals("Hard",c.toString())

    }
}