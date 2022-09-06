package service

import entity.Maturity
import entity.AnimalType
import entity.Card
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private const val CARDS_FILE = "/Pokémon Cards New.png"
private const val IMG_HEIGHT = 90
private const val IMG_WIDTH = 90

/**
 * Provides access to the src/main/resources/card_deck.png file that contains all card images
 * in a raster. The returned [BufferedImage] objects of [frontImageFor],
 * and [backImage] are 130x200 pixels.
 */
class CardImageLoader {

    /**
     * The full raster image containing the suits as rows (plus one special row for blank/back)
     * and values as columns (starting with the ace). As the ordering does not correctly reflect
     * the order in which the suits are declared in [AnimalType], mappings via [row] and [column]
     * are required.
     */
    private val image : BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(CARDS_FILE))

    /**
     * Provides the card image for the given [AnimalType] and [Maturity]
     */
    fun frontImageFor(animalType: AnimalType, maturity: Maturity) =
        getImageByCoordinates(maturity.column, animalType.row)

    /**
     * returns the image for a card
     */
    fun getImageForCard(c: Card)  : BufferedImage {
        return when(c){
            is Card.Coin -> coin
            is Card.Animal -> frontImageFor(c.animalType, c.maturity)
            is Card.Shop -> when(c.shopType) {
                1 -> vendingStall1
                2 -> vendingStall2
                3 -> vendingStall3
                else -> vendingStall4
            }
        }
    }

    /*
    private fun stringToAnimalType(s:String) : AnimalType{
        var out = AnimalType.Flamingo
        when(s){
            "Kamel"       -> out =  AnimalType.Kamel
            "Leopard"     -> out = AnimalType.Leopard
            "Elefant"     -> out = AnimalType.Elefant
            "Panda"       -> out = AnimalType.Panda
            "Schimpanse"  -> out = AnimalType.Schimpanse
            "Zebra"       -> out = AnimalType.Zebra
            "Kaenguru"    -> out = AnimalType.Kaenguru
        }
        return out
    }

    private fun stringToMaturity(s:String) : Maturity{
        var out = Maturity.Male
        when(s){
            "Female"        -> out =  Maturity.Female
            "Offspring"     -> out = Maturity.OffSpring
            "Neutral"       -> out = Maturity.Neutral
        }
        return out
    }

     */



    /**
     * Provides vending Stall1 card
     */
    private val vendingStall1 : BufferedImage get() = getImageByCoordinates(0, 8)

    /**
     * Provides vending Stall2 card
     */
    private val vendingStall2 : BufferedImage get() = getImageByCoordinates(1, 8)

    /**
     * Provides vending Stall3 card
     */
    private val vendingStall3 : BufferedImage get() = getImageByCoordinates(2, 8)

    /**
     * Provides vending Stall4 card
     */
    private val vendingStall4 : BufferedImage get() = getImageByCoordinates(3, 8)

    /**
     * Provides coin card
     */
    private val coin : BufferedImage get() = getImageByCoordinates(0, 9)

    /**
     * Provides the back side image of the card deck
     */
    val backImage: BufferedImage get() = getImageByCoordinates(1, 9)

    /**
     * retrieves from the full raster image [image] the corresponding sub-image
     * for the given column [x] and row [y]
     *
     * @param x column in the raster image, starting at 0
     * @param y row in the raster image, starting at 0
     *
     */
    private fun getImageByCoordinates (x: Int, y: Int) : BufferedImage =
        image.getSubimage(
            x * IMG_WIDTH,
            y * IMG_HEIGHT,
            IMG_WIDTH,
            IMG_HEIGHT
        )

}

/**
 * As the [CARDS_FILE] does not have the same ordering of suits
 * as they are in [AnimalType], this extension property provides
 * a corresponding mapping to be used when addressing the row.
 *
 */
private val AnimalType.row get() = when (this) {
    AnimalType.Panda        -> 0
    AnimalType.Zebra        -> 1
    AnimalType.Elefant      -> 2
    AnimalType.Schimpanse   -> 3
    AnimalType.Flamingo     -> 4
    AnimalType.Kamel        -> 5
    AnimalType.Leopard      -> 6
    AnimalType.Kaenguru    -> 7
}


/**
 * As the [CARDS_FILE] does not have the same ordering of values
 * as they are in [Maturity], this extension property provides
 * a corresponding mapping to be used when addressing the column.
 */
private val Maturity.column get() = when (this) {
    Maturity.Neutral        -> 0
    Maturity.Female         -> 1
    Maturity.Male           -> 2
    Maturity.OffSpring      -> 3
}
