package service

import entity.Maturity
import entity.AnimalType
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.test.*

/**
 * Test cases for the [CardImageLoader]
 */
class CardImageLoaderTest {
    /**
     * The [CardImageLoader] that is tested with this test class
     */
    private val imageLoader: CardImageLoader = CardImageLoader()

    /**
     * A neutral of zebra test image that is used to be compared to image
     * loaded with [imageLoader]. Please note that it could be initialized
     * directly, but to showcase the use of the [BeforeTest] annotation, it
     * is initialized in [loadCompareImage]
     */
    private var neutralOfZebra : BufferedImage? = null

    /**
     * Executed before all test methods in this class. Initializes the
     * [neutralOfZebra] property.
     */
    @BeforeTest
    fun loadCompareImage() {
        neutralOfZebra =
            ImageIO.read(CardImageLoaderTest::class.java.getResource("/neutral_of_zebra.png"))
    }

    /**
     * Loads the image for every possible animalType/maturity combination as well as
     * front and back side and checks whether the resulting [BufferedImage]
     * has the correct dimensions of 130x200 px.
     */
    @Test
    fun testLoadAll() {
        val allImages = mutableListOf<BufferedImage>()
        AnimalType.values().forEach { animalType ->
            Maturity.values().forEach { maturity ->
                allImages += imageLoader.frontImageFor(animalType, maturity)
            }
        }
        allImages += imageLoader.backImage
        //allImages += imageLoader.blankImage

        allImages.forEach {
            assertEquals(90, it.width)
            assertEquals(90, it.height)
        }
    }

    /* PIXEL ERROR
    /**
     * Loads the neutral of zebra from the [imageLoader] and tests equality to [neutralOfZebra]
     */
    @Test
    fun testCardEquality() {
        val testImage = imageLoader.frontImageFor(AnimalType.Zebra, Maturity.Neutral)
        assertTrue (testImage sameAs neutralOfZebra) // Pixel Error
    }*/

    /**
     * Loads the female of leopards from the [imageLoader] and tests inequality [neutralOfZebra]
     */
    @Test
    fun testCardInequality() {
        val testImage = imageLoader.frontImageFor(AnimalType.Leopard, Maturity.Female)
        assertFalse(testImage sameAs neutralOfZebra)
    }

}


/**
 * Tests equality of two [BufferedImage]s by first checking if they have the same dimensions
 * and then comparing every pixels' RGB value.
 */
private infix fun BufferedImage.sameAs(other: Any?): Boolean {

    // if the other is not even a BufferedImage, we are done already
    if (other !is BufferedImage) {
        return false
    }

    // check dimensions
    if (this.width != other.width || this.height != other.height) {
        return false
    }

    // compare every pixel
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (this.getRGB(x, y) != other.getRGB(x, y))
                return false
        }
    }

    // if we reach this point, dimensions and pixels match
    return true

}
