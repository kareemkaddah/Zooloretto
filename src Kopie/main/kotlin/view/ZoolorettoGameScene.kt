package view

import entity.*
import service.CardImageLoader
import service.MoneyAction
import service.RootService
import service.Turn
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.dialog.*
import tools.aqua.bgw.event.DragEvent
import tools.aqua.bgw.event.DropEvent
import tools.aqua.bgw.event.MouseEvent
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import java.awt.Color

/**
 * This is the main thing for the War game. The scene shows the complete table at once.
 * Player 1 "sits" is on the bottom half of the screen, player 2 on the top.
 * Each player has four areas for their cards: left and right draw stack, played cards, and collected
 * cards. Clicking on either draw stack triggers the corresponding player's draw left/right action.
 */
class ZoolorettoGameScene(private val app: BoardGameApplication, private val rootService: RootService) :
    BoardGameScene(1920, 1080), Refreshable {

    /**
     * UI improver.
     */
    private val improvedUI: ImprovedUI = ImprovedUI()
    private val tooltipViewer: Label = Label(
        height = 50, visual = CompoundVisual(ImageVisual("images/Bubble.png"))
    ).apply {
        isVisible = false
        isFocusable = false
    }

    private val cardImageLoader = CardImageLoader()

    /**
     * Enclosures
     */

    //Expansion 1
    private val positionsAnimalsExp1 = listOf(51, 219, 376, 540, 699)
    private val animalsExp1 = positionsAnimalsExp1.map {
        Area<TokenView>(
            width = 110,
            height = 113,
            posX = 174,
            posY = it,
            visual = ImageVisual("Ground_tile_lighter.PNG")
        )
    }
    private val shopsExp1 = listOf(
        Area<TokenView>(
            width = 110,
            height = 110,
            posX = 174,
            posY = 917,
            visual = ImageVisual("Ground_tile_dark.PNG")
        )
    )
    private val redDottedAnimalsExp1 = animalsExp1.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }
    private val redDottedShopsExp1 = shopsExp1.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }

    //Expansion 2
    private val positionsAnimalsExp2 = positionsAnimalsExp1
    private val animalsExp2 = positionsAnimalsExp2.map {
        Area<TokenView>(width = 110, height = 113, posX = 0, posY = it, visual = ImageVisual("Ground_tile_lighter.PNG"))
    }
    private val shopsExp2 = listOf(
        Area<TokenView>(
            width = 110,
            height = 110,
            posX = 20,
            posY = 917,
            visual = ImageVisual("Ground_tile_dark.PNG")
        )
    )
    private val redDottedAnimalsExp2 = animalsExp2.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }
    private val redDottedShopsExp2 = shopsExp2.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }

    //Top Right enclosure (4 Pokémon, 2 shops)
    private val animalsTopRight: List<Area<TokenView>> = listOf(
        Area(width = 110, height = 110, posX = 975, posY = 68, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 920, posY = 249, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 1174, posY = 165, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 1134, posY = 326, visual = ImageVisual("Ground_tile_lighter.PNG"))
    )
    private val shopsTopRight: List<Area<TokenView>> = listOf(
        Area(width = 120, height = 118, posX = 850, posY = 47, visual = ImageVisual("Ground_tile_dark.PNG")),
        Area(width = 120, height = 118, posX = 855, posY = 430, visual = ImageVisual("Ground_tile_dark.PNG"))
    )
    private val redDottedAnimalsTopRight = animalsTopRight.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }
    private val redDottedShopsTopRight = shopsTopRight.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }

    //Bottom Right enclosure (6 Pokémon, 1 Shop)
    private val animalsBottomRight: List<Area<TokenView>> = listOf(
        Area(width = 110, height = 110, posX = 844, posY = 743, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 1017, posY = 751, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 1169, posY = 716, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 1119, posY = 832, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 915, posY = 924, visual = ImageVisual("Ground_tile_lighter.PNG")),
        Area(width = 110, height = 110, posX = 1244, posY = 826, visual = ImageVisual("Ground_tile_lighter.PNG"))
    )
    private val shopsBottomRight = listOf(
        Area<TokenView>(
            width = 120,
            height = 118,
            posX = 1169,
            posY = 962,
            visual = ImageVisual("Ground_tile_dark.PNG")
        )
    )
    private val redDottedAnimalsBottomRight = animalsBottomRight.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }
    private val redDottedShopsBottomRight = shopsBottomRight.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }

    //Top Left enclosure
    private val animalsTopLeft: List<Area<TokenView>> = listOf(
        Area(width = 110, height = 110, posX = 633, posY = 106, visual = ImageVisual("Ground_tile_snow.PNG")),
        Area(width = 110, height = 110, posX = 441, posY = 216, visual = ImageVisual("Ground_tile_snow.PNG")),
        Area(width = 110, height = 110, posX = 606, posY = 278, visual = ImageVisual("Ground_tile_snow.PNG")),
        Area(width = 110, height = 110, posX = 441, posY = 373, visual = ImageVisual("Ground_tile_snow.PNG")),
        Area(width = 110, height = 110, posX = 653, posY = 430, visual = ImageVisual("Ground_tile_snow.PNG"))
    )
    private val shopsTopLeft = listOf(
        Area<TokenView>(
            width = 120,
            height = 118,
            posX = 337,
            posY = 0,
            visual = ImageVisual("Ground_tile_dark.PNG")
        )
    )
    private val redDottedAnimalsTopLeft = animalsTopLeft.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }
    private val redDottedShopsTopLeft = shopsTopLeft.map {
        Area<TokenView>(
            width = it.width,
            height = it.height,
            posX = it.posX,
            posY = it.posY,
            visual = ImageVisual("shape.png")
        )
    }

    private val allCardLabels = (
            animalsExp1 + shopsExp1 +
                    animalsExp2 + shopsExp2 +
                    animalsTopRight + shopsTopRight +
                    animalsBottomRight + shopsBottomRight +
                    animalsTopLeft + shopsTopLeft
            )

    private val allRedDotted = (
            redDottedAnimalsExp1 + redDottedShopsExp1 +
                    redDottedAnimalsExp2 + redDottedShopsExp2 +
                    redDottedAnimalsTopRight + redDottedShopsTopRight +
                    redDottedAnimalsBottomRight + redDottedShopsBottomRight +
                    redDottedAnimalsTopLeft + redDottedShopsTopLeft
            )

    private val allShopsAsLists = listOf(
        listOf(),
        shopsBottomRight,
        shopsTopLeft,
        shopsTopRight,
        shopsExp1,
        shopsExp2
    )

    private val allAnimalsAsLists = listOf(
        listOf(),
        animalsBottomRight,
        animalsTopLeft,
        animalsTopRight,
        animalsExp1,
        animalsExp2
    )

    private val allShops = allShopsAsLists.flatten()
    private val allAnimals = allAnimalsAsLists.flatten()

    /**
     * Expansion1 = 1
     * Expansion2 = 2
     * TopLeft = 3
     * TopRight = 4
     * BottomRight = 5
     *
     */
    private fun componentToEnclosure(obj: Any): Pair<Int, Int> {
        return when (true) {
            animalsBottomRight.contains(obj) -> Pair(1, animalsBottomRight.indexOf(obj))
            shopsBottomRight.contains(obj) -> Pair(1, shopsBottomRight.indexOf(obj))
            animalsTopLeft.contains(obj) -> Pair(2, animalsTopLeft.indexOf(obj))
            shopsTopLeft.contains(obj) -> Pair(2, shopsTopLeft.indexOf(obj))
            animalsTopRight.contains(obj) -> Pair(3, animalsTopRight.indexOf(obj))
            shopsTopRight.contains(obj) -> Pair(3, shopsTopRight.indexOf(obj))
            animalsExp1.contains(obj) -> Pair(4, animalsExp1.indexOf(obj))
            shopsExp1.contains(obj) -> Pair(4, shopsExp1.indexOf(obj))
            animalsExp2.contains(obj) -> Pair(5, animalsExp2.indexOf(obj))
            shopsExp2.contains(obj) -> Pair(5, shopsExp2.indexOf(obj))
            else -> Pair(0, 0)
        }
    }

    /**
     * General Variables
     */

    private lateinit var gameMaster: GameMaster
    private lateinit var currentGame: ZoolorettoGame
    private lateinit var currentPlayer: Player
    private lateinit var players: List<Player>
    private var playerSize = 0

    private val barnMap: MutableMap<TokenView, Card> = mutableMapOf()
    private val emptyEnclosureSpacesIndices: MutableList<Int> = mutableListOf()
    private var turnTodo: Turn? = null
    private var currentlyScoutingPlayer: Player? = null
    private var alreadyDroppedInBarn: Int = 0
    private lateinit var purchaseSPlayer: Player
    private lateinit var purchaseChosenCard: Card

    /**
     * Boolean Values
     */

    private var currentlyDuringATurn: Boolean = false
    private var currentlyScouting: Boolean = false

    private fun exp1BuyVisibleAndBuyable(): Boolean {
        return !currentlyScouting && !historyView.isVisible && !currentlyDuringATurn && (currentGame.currentPlayer.balance > 2
                && ((playerSize == 2 && currentPlayer.zoo.expanded == 2)
                || (playerSize > 2 && currentPlayer.zoo.expanded == 1)))
    }

    private fun exp2BuyVisibleAndBuyable(): Boolean {
        return !currentlyScouting && !historyView.isVisible && !currentlyDuringATurn && (currentPlayer.balance > 2 &&
                playerSize == 2 &&
                currentPlayer.zoo.expanded == 1)
    }

    /**
     * Truck Stuff
     */

    private var generatedTrucks: List<Triple<Label, List<Label>, List<Area<TokenView>>>> = listOf()

    private var trucksCardLabels: List<Area<TokenView>> = listOf()
    private var trucksRedDottedCardsLabels: List<Label> = listOf()
    private var truckLabels: List<Label> = listOf()
    private var trucksWholeRedDottedLabels: List<Label> = listOf()

    private var selectedTruckCards: List<Area<TokenView>> = listOf()
    private var selectedTruck = Truck()
    private var alreadyTakenTrucks: MutableList<Int> = mutableListOf()
    private var takingTruckMap: MutableList<Triple<Card, Enclosure?, Int>> = mutableListOf()

    private fun generateAllTrucks() {
        val truckPositions: List<Int> =
            listOf(0, 135, 270, 405, 540).mapIndexed { index, i -> (index*20)+i+30 }.take(currentGame.trucks.size)
        generatedTrucks = truckPositions.map { generateOneTruck(1500, it) }
        truckLabels = generatedTrucks.map { it.first }
        trucksRedDottedCardsLabels = generatedTrucks.map { it.second }.flatten()
        trucksCardLabels = generatedTrucks.map { it.third }.flatten()

        trucksWholeRedDottedLabels = truckLabels.map { truck ->
            Label(
                width = truck.width,
                height = truck.height,
                posX = truck.posX,
                posY = truck.posY,
                visual = ImageVisual("selectedTruck.png")
            ).also {
                it.isVisible = false
                it.isFocusable = false
                it.onMouseExited = { _ ->
                    it.isVisible = false
                }
                it.onMouseClicked = { _ -> takeTruckClicked(it) }
            }
        }

        truckLabels.forEach { truck ->
            addComponents(truck)
            truck.onMouseEntered = {
                trucksWholeRedDottedLabels[truckLabels.indexOf(truck)].isVisible =
                     currentGame.trucks[truckLabels.indexOf(truck)].cards.filterNotNull().isNotEmpty()
                            && !currentlyDuringATurn
                            && !showDrawnCard.isVisible
                            && !currentlyScouting
            }
            addComponents(trucksWholeRedDottedLabels[truckLabels.indexOf(truck)])
        }

        trucksRedDottedCardsLabels.forEach { addComponents(it); it.isVisible = false }
        trucksCardLabels.forEach {
            addComponents(it)
            it.dropAcceptor = droppedOnTruck(it)
        }
    }

    private fun generateOneTruck(x: Int, y: Int): Triple<Label, List<Label>, List<Area<TokenView>>> {
        val posx1 = x + 20
        val posx2 = posx1 + 90 + 25
        val posx3 = posx2 + 90 + 25

        val posy = y + 20

        return Triple(
            first = Label(width = 360, height = 135, posX = x, posY = y, visual = ImageVisual("truck.png")),
            second = listOf(
                Label(width = 100, height = 90, posX = posx1, posY = posy, visual = ImageVisual("dotted-square.png")),
                Label(width = 100, height = 90, posX = posx2, posY = posy, visual = ImageVisual("dotted-square.png")),
                Label(width = 100, height = 90, posX = posx3, posY = posy, visual = ImageVisual("dotted-square.png"))
            ),
            third = listOf(
                Area(width = 90, height = 90, posX = posx1, posY = posy, visual = Visual.EMPTY),
                Area(width = 90, height = 90, posX = posx2, posY = posy, visual = Visual.EMPTY),
                Area(width = 90, height = 90, posX = posx3, posY = posy, visual = Visual.EMPTY)
            )
        )
    }

    private fun redDottedShowTrucks() {
        val trucksZipped = trucksRedDottedCardsLabels.zip(currentGame.trucks.map { it.cards }.flatten())
        for (e in trucksZipped) e.first.isVisible = (e.second == null)
        alreadyTakenTrucks.forEach {
            trucksZipped[it * 3].first.isVisible = false
            trucksZipped[1 + it * 3].first.isVisible = false
            trucksZipped[2 + it * 3].first.isVisible = false

            trucksCardLabels[it * 3].isVisible = false
            trucksCardLabels[1 + it * 3].isVisible = false
            trucksCardLabels[2 + it * 3].isVisible = false
        }

        if (playerSize == 2) {
            trucksZipped[5].first.isVisible = false
            trucksZipped[7].first.isVisible = false
            trucksZipped[8].first.isVisible = false
        }
    }

    private fun disableDraw() {
        drawButton.isDisabled = true
    }

    private fun disableRedDraw() {
        redDrawButton.isDisabled = true
    }

    private fun enableDraw() {
        drawButton.isDisabled = false
    }

    private fun enableRedDraw() {
        redDrawButton.isDisabled = false
    }

    /**
     * Other Buttons
     */
    private val discardLabel = Label(
        width = 100,
        height = 100,
        posX = 1420,
        posY = 950,
        visual = ImageVisual("trash.png"),
        alignment = tools.aqua.bgw.core.Alignment.BOTTOM_CENTER,
        font = Font(20)
    )
    private val discardArea = Area<TokenView>(width = discardLabel.width, height = discardLabel.height, posX = discardLabel.posX, posY = discardLabel.posY, visual = Visual.EMPTY)

    private val deckLabel =
        Label(width = 110, height = 110, posX = 1420, posY = 800, visual = ImageVisual(cardImageLoader.backImage))


    private val buyLabel = Label(
        width = 100,
        height = 100,
        posX = 1570,
        posY = 950,
        visual = ImageVisual("buy.png"),
        font = Font(20)
    )
    private val buyArea = Area<TokenView>(width = buyLabel.width, height = buyLabel.height, posX = buyLabel.posX, posY = buyLabel.posY, visual = Visual.EMPTY)

    private val extraCardsLabel =
        Label(width = deckLabel.width, height = deckLabel.height, posX = 1570, posY = deckLabel.posY, visual = ImageVisual(cardImageLoader.backImage))

    private val redDiscOnExtraCards = Label(
            width = extraCardsLabel.width-15,
            height = extraCardsLabel.height-15,
            posX = extraCardsLabel.posX+8,
            posY = extraCardsLabel.posY+10,
            font = Font(size = 20),
        visual = ImageVisual("redDisc.png")
        )

    private val deckCardsAmountLabel =
        Label(width = 150, height = 10, posX = deckLabel.posX-25, posY = deckLabel.posY+110, font = Font(size = 16, fontWeight = Font.FontWeight.BOLD))

    private val extraCardsAmountLabel =
        Label(width = 150, height = 10, posX = extraCardsLabel.posX-25, posY = extraCardsLabel.posY+110, font = Font(size = 16, fontWeight = Font.FontWeight.BOLD))

    private val currentPlayerLabel =
        Label(width = 150, height = 200, posX = 1755, posY = 840, visual = Visual.EMPTY, font = Font(size = 35, fontWeight = Font.FontWeight.BOLD))
    private val balancePlayerNumberLabel =
        Label(
            width = currentPlayerLabel.width,
            height = currentPlayerLabel.height,
            posX = currentPlayerLabel.posX-20,
            posY = currentPlayerLabel.posY+55,
            visual = Visual.EMPTY,
            font = Font(size = 40)
        )
    private val balancePlayerCoinsLabel =
        Label(
            width = 50,
            height = 50,
            posX = balancePlayerNumberLabel.posX+100,
            posY = balancePlayerNumberLabel.posY+80,
            visual = ImageVisual("images/MoneyActions.png"),
            font = Font(size = 30)
        )
    private val balanceBankLabel =
        Label(
            width = 200,
            height = 20,
            posX = 1738,
            posY = 1044,
            visual = Visual.EMPTY,
            font = Font(size = 15)
        )



    private val saveButton = Button(
        width = 60, height = 60, posX = 625.5, posY = 10, visual = ImageVisual("images/save_hires.png")
    ).apply {
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(
                component = this,
                message = "Save game"
            )
        }
        onMouseExited = { improvedUI.hideTooltip() }
        onMouseClicked = saveButtonClicked()
    }

    private val hintButton = Button(
        width = 60, height = 60, posX = 685.5, posY = 10, visual = ImageVisual("lightbulb.png")
    ).apply {
        onMouseEntered = {
            if (!isDisabled) {
                visual = ImageVisual("lightbulbhover.png")
            }
            tooltipViewer.visual = improvedUI.showTooltipVisual(
                component = this,
                message = "Show hint"
            )
        }
        onMouseExited = {
            if (!isDisabled) {
                visual = ImageVisual("lightbulb.png")
            }
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            val game = rootService.gameMaster.currentGame
            rootService.playerActionService.game = game
            val turn = rootService.kiServiceV2.getBestTurn(game)
            val hint = "you can " + when (turn) {
                is Turn.Draw -> "draw a tile and place it on truck #${game.trucks.indexOf(turn.truck)}."
                is Turn.TakeTruck -> "take truck #${game.trucks.indexOf(turn.truck)}."
                is Turn.MoneyAction -> when (turn.m) {
                    is MoneyAction.Move1Token -> "move ${turn.m.chosenCard} from your barn" +
                            " to enclosure with the capacity ${turn.m.moveLocation.animalCapacity}."
                    is MoneyAction.ExpandZoo -> "expand your zoo."
                    is MoneyAction.Swap2Types -> {
                        "swap ${turn.m.map.values.first().name}" +
                                " in enclosure with capacity" +
                                " ${turn.m.map.keys.first().animalCapacity} " +
                                "with ${turn.m.map.values.last().name} in" +
                                " enclosure with capacity ${turn.m.map.keys.last().animalCapacity}."
                    }
                    is MoneyAction.Purchase1Token -> "purchase ${turn.m.chosenAnimal}" +
                            " from ${turn.m.sPlayer.getPlayerName()}" +
                            "and place it in enclosure with capacity" +
                            " ${turn.m.moveLocation.animalCapacity}."
                    is MoneyAction.Discard1Token -> "discard ${turn.m.chosenCard} from your barn."
                    is MoneyAction.Skip -> "skip."
                }
            }
            app.showDialog(
                Dialog(
                    dialogType = DialogType.INFORMATION,
                    title = "Show hint",
                    header = "Hint!",
                    message = hint
                )
            )
        }
    }

    private val historyView = ListView<String>(
        width = 500, height = 360, posX = 550, posY = 150, visual = ImageVisual("historybg.png"),
        font = Font(size = 24), selectionMode = SelectionMode.MULTIPLE, selectionBackground = ColorVisual(252, 215, 3, 100)
    ).apply { isVisible = false; isFocusable = false }

    private val historyButton = Button(
        posX = 888.5, posY = 12, width = 70, height = 70, visual = ImageVisual("history.png")
    ).apply {
        isDisabled = true
        onMouseEntered = {
            if (!isDisabled) {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    component = this,
                    message = "Show history"
                )
            }
        }
        onMouseExited = {
            if (!isDisabled) {
                improvedUI.hideTooltip()
            }
        }
        onMouseClicked = {
            historyView.isVisible = !historyView.isVisible
            historyView.isFocusable = !historyView.isFocusable
        }
    }

    private val undoButton = Button(
        width = 60, height = 60, posX = 755.5, posY = 10, visual = ImageVisual("undo.png")
    ).apply {
        isDisabled = true
        opacity = 0.5
        onMouseEntered = {
            if (!isDisabled) {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    component = this,
                    message = "Undo"
                )
            }
        }
        onMouseExited = {
            if (!isDisabled) {
                improvedUI.hideTooltip()
            }
        }
        onMouseClicked = {
            rootService.gameMaster.undo()
            refreshAfterStartNewGame()
        }
    }

    private val redoButton = Button(
        width = 60, height = 60, posX = 825.5, posY = 10, visual = ImageVisual("redo.png")
    ).apply {
        isDisabled = true
        opacity = 0.5
        onMouseEntered = {
            if (!isDisabled) {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    component = this,
                    message = "Redo"
                )
            }
        }
        onMouseExited = {
            if (!isDisabled) {
                improvedUI.hideTooltip()
            }
        }
        onMouseClicked = {
            rootService.gameMaster.redo()
            refreshAfterStartNewGame()
        }
    }

    private val board = Label(
        width = 1746,
        height = 1080,
        posX = 175,
        posY = 0,
        visual = ImageVisual("BackgroundNew.png")
    )

    private var showDrawnCard: TokenView = TokenView(
        width = 90,
        height = 90,
        posX = deckLabel.posX+10,
        posY = deckLabel.posY+10,
        visual = Visual.EMPTY
    ).apply {
        isDraggable = true
        isVisible = false
        isFocusable = false
    }
    private var showEndDrawnCard: TokenView = TokenView(
        width = extraCardsLabel.width,
        height = extraCardsLabel.height,
        posX = extraCardsLabel.posX,
        posY = extraCardsLabel.posY,
        visual = Visual.EMPTY
    ).apply {
        isDraggable = true
        isVisible = false
        isFocusable = false
    }

    private var showBoughtCard: TokenView = TokenView(
        width = 90,
        height = 90,
        posX = buyArea.posX + 100,
        posY = buyArea.posY,
        visual = Visual.EMPTY
    ).apply { isDraggable = true }


    private val bigEnclosureInfo = Label(
        width = 200,
        height = 100,
        posX = 1160,
        posY = 900,
        visual = Visual.EMPTY,
        text = "10/6; 0 Coins",
        font = Font(size = 20)
    )
    private val smallEnclosureInfo = Label(
        width = 200,
        height = 100,
        posX = 1175,
        posY = 465,
        visual = Visual.EMPTY,
        text = "5/4; 1 Coin",
        font = Font(size = 20)
    )
    private val mediumEnclosureInfo = Label(
        width = 200,
        height = 100,
        posX = 550,
        posY = 35,
        visual = Visual.EMPTY,
        text = "8/5; 2 Coins",
        font = Font(size = 20)
    )
    private val expansionEnclosureInfo = Label(
        width = 200,
        height = 100,
        posX = 160,
        posY = -25,
        visual = Visual.EMPTY,
        text = "   9/5; \n 1 Coin",
        font = Font(size = 20)
    )
    private val expansion1EnclosureInfo = Label(
        width = 200,
        height = 100,
        posX = -10,
        posY = -25,
        visual = Visual.EMPTY,
        text = "   9/5; \n 1 Coin",
        font = Font(size = 20)
    )

    private val exp1BuyButton =
        Label(width = 174, height = 1080, posX = 150, posY = 0, visual = ImageVisual("Buy_Expansion_2.png")).apply {
            isVisible = false
            onMouseEntered = { isVisible = exp1BuyVisibleAndBuyable() }
            onMouseExited = { isVisible = false }
            onMouseClicked = {
                if (exp1BuyVisibleAndBuyable()) rootService.gameService.turn(Turn.MoneyAction(MoneyAction.ExpandZoo))
                isVisible = false
            }
        }
    private val exp1 = Label(width = 174, height = 1080, posX = 150, posY = 0, visual = Visual.EMPTY).apply {
        onMouseEntered = { exp1BuyButton.isVisible = exp1BuyVisibleAndBuyable() }
    }

    private val exp2BuyButton =
        Label(width = 174, height = 1080, posX = 0, posY = 0, visual = ImageVisual("Buy_Expansion.png")).apply {
            isVisible = false
            onMouseEntered = {
                isVisible = exp2BuyVisibleAndBuyable()
            }
            onMouseExited = { isVisible = false }
            onMouseClicked = {
                if (exp2BuyVisibleAndBuyable()) rootService.gameService.turn(Turn.MoneyAction(MoneyAction.ExpandZoo))
                isVisible = false
            }
        }
    private val exp2 = Label(
        width = 174,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ImageVisual("Zooloretto_Erweiterung_einzeln.PNG")
    ).apply {
        onMouseEntered = { exp2BuyButton.isVisible = exp2BuyVisibleAndBuyable() }
    }


    private val barnLinearLayouts: List<LinearLayout<TokenView>> = listOf(700, 790, 880, 970).map {
        LinearLayout(
            posX = 334, posY = it,
            width = 504, height = 100,
            visual = Visual.EMPTY, spacing = -20
        )
    }

    private val playerButtons: List<Button> = (0..4).map {
        Button(
            width = 175, height = 80, posX = 0, posY = 1000, font = Font(size = 20),
            visual = ColorVisual(225, 213, 231)
        ).apply {
            //onMouseEntered = playerButtonsMouse(this, it, "images/PlayerLabelHover.png")
            //onMouseExited = playerButtonsMouse(this, it, "images/PlayerLabel.png")
            onMouseClicked = { _: MouseEvent -> scoutClicked(this, players[it]) }
        }
    }

    private val settings =
        Button(width = 60, height = 35, posX = 755.5, posY = 0, visual = ImageVisual("04 - Arrow Down.png")).apply {
            onMouseEntered = {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    component = this,
                    message = "Show / hide options"
                )
            }
            onMouseExited = { improvedUI.hideTooltip() }
            onMouseClicked = {
                if (this.posY == 0.0) {
                    posY = 80.0
                    visual = ImageVisual("03 - Arrow Up.png")
                    hintButton.isDisabled = false
                    undoButton.isDisabled = false
                    redoButton.isDisabled = false
                    saveButton.isDisabled = false
                    historyButton.isDisabled = false
                    hintButton.isVisible = true
                    undoButton.isVisible = true
                    redoButton.isVisible = true
                    saveButton.isVisible = true
                    historyButton.isVisible = true

                    /* bring elements to front */
                    moveToFront(
                        this, saveButton, hintButton, undoButton, redoButton, tooltipViewer, historyButton, historyView
                    )
                } else {
                    posY = 0.0
                    visual = ImageVisual("04 - Arrow Down.png")
                    historyView.isVisible = false
                    historyView.isFocusable = false
                    hintButton.isDisabled = true
                    undoButton.isDisabled = true
                    redoButton.isDisabled = true
                    historyButton.isDisabled = true
                    hintButton.isVisible = false
                    undoButton.isVisible = false
                    redoButton.isVisible = false
                    saveButton.isVisible = false
                    historyButton.isVisible = false
                    moveToFront(tooltipViewer)
                }
            }
        }

    private val playerMenuArrow =
        Button(width = 60, height = 35, posX = 755.5, posY = 1045, visual = ImageVisual("03 - Arrow Up.png")).apply {
            onMouseEntered = {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    component = this,
                    message = "Show / hide players"
                )
            }
            onMouseExited = { improvedUI.hideTooltip() }
            onMouseClicked = playerMenuArrowClicked(this)
        }

    private var drawButton : Button = Button(
        width = deckLabel.width,
        height = deckLabel.height,
        posX = deckLabel.posX,
        posY = deckLabel.posY,
        font = Font(size = 24,color = Color(255,255,255), fontWeight = Font.FontWeight.BOLD,family = "Pokemon Solid"),
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            if (!isDisabled) {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    this, "Click to draw. Drag to place."
                )
            }
        }
        onMouseExited = { if (!isDisabled) improvedUI.hideTooltip() }
        onMouseClicked = onDrawClicked(this)
    }

    private val redDrawButton = Button(
        width = extraCardsLabel.width,
        height = extraCardsLabel.height,
        posX = extraCardsLabel.posX,
        posY = extraCardsLabel.posY,
        font = Font(size = 20),
        visual = Visual.EMPTY
    ).apply {
        onMouseEntered = {
            if (!isDisabled) {
                tooltipViewer.visual = improvedUI.showTooltipVisual(
                    this, "Draw 1 tile and place it on a couch"
                )
            }
        }
        onMouseExited = { if (!isDisabled) improvedUI.hideTooltip() }
        onMouseClicked = onDrawClicked(this)
    }

    init {
        background = ColorVisual(213, 232, 212, 255)

        addComponents(board)
        addComponents(tooltipViewer, settings, exp1, exp2, hintButton, undoButton, saveButton, redoButton)

        improvedUI.tooltipViewer = tooltipViewer

        barnLinearLayouts.forEach {
            it.apply {
                onMouseEntered = { _ ->
                    components.forEach { it.isDraggable = true }
                }
            }
            addComponents(it)
            it.dropAcceptor = droppedOnBarn(it).also{redoTurn()}
        }

        allCardLabels.forEach {
            it.dropAcceptor = droppedOnAnyEnclosureSpace(it)
            it.onDragGestureEnded = {_ : DropEvent , _ : Boolean-> redoTurn() }
        }

        allRedDotted.forEach {
            it.isVisible = false
            it.isFocusable = false
            it.also {
                it.dropAcceptor = droppedOnAnyEnclosureSpaceRedDotted(it)
            }
        }

        discardArea.also {
            it.dropAcceptor = droppedOnDiscardArea()
            it.onDragGestureExited = {_ : DragEvent -> redoTurn() }
        }

        buyArea.also {
            it.dropAcceptor = droppedOnBuyArea()
            it.onDragGestureExited = {_ : DragEvent -> redoTurn() }
        }

        addComponents(extraCardsLabel, redDiscOnExtraCards, currentPlayerLabel,balancePlayerCoinsLabel,balancePlayerNumberLabel,balanceBankLabel,extraCardsAmountLabel,deckCardsAmountLabel)
        addComponents(exp1BuyButton, exp2BuyButton)
        addComponents(deckLabel, drawButton, discardLabel, buyLabel, discardArea, buyArea)
        addComponents(playerMenuArrow, showDrawnCard, showEndDrawnCard, showBoughtCard)
        addComponents(
            bigEnclosureInfo,
            smallEnclosureInfo,
            mediumEnclosureInfo,
            expansionEnclosureInfo,
            expansion1EnclosureInfo,
            historyView
        )

        hintButton.isDisabled = true
        undoButton.isDisabled = true
        redoButton.isDisabled = true
        saveButton.isDisabled = true
        hintButton.isVisible = false
        undoButton.isVisible = false
        redoButton.isVisible = false
        saveButton.isVisible = false
    }

    /**
     * Refreshes
     */

    private fun updateTrashAndBuy(){
        buyLabel.also{
            it.isDisabled = currentlyScouting
            it.opacity = if(currentlyScouting) 1.0 else 0.5
        }
        discardLabel.also{
            it.isDisabled = !currentlyScouting
            it.opacity = if(currentlyScouting) 0.5 else 1.0
        }
    }

    override fun refreshAfterRoundEnd() {
        alreadyTakenTrucks = mutableListOf()
    }

    override fun refreshAfterStartNewGame() {
        refreshAfterTurn()
    }

    override fun refreshAfterTurn() {
        gameMaster = rootService.gameMaster
        currentGame = gameMaster.currentGame
        players = currentGame.players
        currentPlayer = currentGame.currentPlayer
        playerSize = players.size
        turnTodo = null

        //currentPlayer.balance = 100

        if(currentPlayer is Player.AIPlayer && !rootService.gameService.gameHasEnded) {
            rootService.playerActionService.game = currentGame
            val turn = rootService.kiServiceV2.getBestTurn(currentGame, (currentPlayer as Player.AIPlayer).difficulty)
            rootService.gameService.turn(turn)
        }else{

            exp1BuyButton.isVisible = false
            exp2BuyButton.isVisible = false
            currentlyDuringATurn = false
            emptyEnclosureSpacesIndices.clear()

            generatedTrucks.forEach {
                removeComponents(it.first)
                it.second.forEach { removeComponents(it) }
                it.third.forEach { removeComponents(it) }
            }
            generateAllTrucks()

            allCardLabels.forEach { it.isDraggable = false }

            /*
            currentPlayerLabel.text =
                "It's ${currentGame.currentPlayer}'s Turn " +
                        " (Balance: ${currentGame.currentPlayer.balance} coins;" +
                        " Bank: ${currentGame.bankCoins})"
             */

            //Truck refreshes
            generatedTrucks.forEach { it.first.isVisible = true }
            val trucksZipped = trucksCardLabels.zip(currentGame.trucks.map { it.cards }.flatten())
            for (e in trucksZipped) {
                e.first.isVisible = true
                e.first.visual = if (e.second != null) {
                    ImageVisual(cardImageLoader.getImageForCard(e.second!!))
                } else {
                    Visual.EMPTY
                }
            }
            currentGame.trucks.forEach { if (!it.aiConsider) alreadyTakenTrucks.add(currentGame.trucks.indexOf(it)) }
            alreadyTakenTrucks.forEach { generatedTrucks[it].first.isVisible = false }

            if (playerSize == 2) {
                trucksZipped[5].first.visual = ImageVisual(cardImageLoader.backImage)
                trucksZipped[7].first.visual = ImageVisual(cardImageLoader.backImage)
                trucksZipped[8].first.visual = ImageVisual(cardImageLoader.backImage)
                trucksZipped[5].first.isDraggable = false
                trucksZipped[7].first.isDraggable = false
                trucksZipped[8].first.isDraggable = false
                trucksZipped[5].first.isVisible = !alreadyTakenTrucks.contains(1)
                trucksZipped[7].first.isVisible = !alreadyTakenTrucks.contains(2)
                trucksZipped[8].first.isVisible = !alreadyTakenTrucks.contains(2)
            }

            val amountNotNullCardsOnTrucks = currentGame.trucks.map { it.cards }.flatten().filterNotNull().size
            val correctedAmountNotNullCardsOnTrucks =
                amountNotNullCardsOnTrucks + players.map {
                    if (it.selectedTruck != null) it.selectedTruck!!.capacity
                    else 0 }
                    .sum()

            if (currentGame.deck.isNotEmpty()) {
                if (correctedAmountNotNullCardsOnTrucks ==
                    currentGame.trucks.sumOf { it.capacity }) disableDraw()
                else enableDraw()
            } else if (correctedAmountNotNullCardsOnTrucks ==
                currentGame.trucks.sumOf { it.capacity }) disableRedDraw()
            else enableRedDraw()
            for (e in players.zip(playerButtons)) {
                val current = e.first == currentPlayer
                e.second.isDisabled = current
                //"images/PlayerLabelDisabled.png"
                val pic = if (current) "images/PlayerLabelTurn.png" else "images/PlayerLabel.png"
                e.second.visual = CompoundVisual(
                    ImageVisual(pic),
                    TextVisual("${e.first}", Font(size = 24))
                )
            }
            refreshPlayerLabels()
            updateTiles(currentPlayer)
            allRedDotted.forEach { it.isVisible = false }

            if(components.contains(historyView)) removeComponents(historyView)
            //historyView = ListView<String>(width = 500, height = 350, posX = 1400, posY = 680, items = currentGame.playedTurns)
            if(currentGame.playedTurns.size > 0) {
                historyView.items.add(currentGame.playedTurns.last())
            }
            addComponents(historyView)
            historyView.isFocusable = false
            historyView.isVisible = false
        }
    }

    /**
     * Will generate al tiles, that are important for Scouting
     */
    private fun updateTiles(forPlayer: Player) {
        updateTrashAndBuy()

        // Refreshes Animals in enclosures
        for (enclosure in forPlayer.zoo.enclosures.zip(allAnimalsAsLists)) {
            for (animal in enclosure.first.animals.zip(enclosure.second)) {
                animal.second.visual =
                    if (animal.first == null) {
                        if (animalsTopLeft.contains(animal.second)) {
                            ImageVisual("Ground_tile_snow.PNG")
                        } else {
                            ImageVisual("Ground_tile_lighter.PNG")
                        }.also { emptyEnclosureSpacesIndices.add(allCardLabels.indexOf(animal.second)) }
                    } else {
                        ImageVisual(cardImageLoader.getImageForCard(animal.first!!)).also {
                            animal.second.apply {
                                isDraggable = true
                            }
                        }
                    }
            }
        }

        // Refreshes Animals in enclosures
        for (enclosure in forPlayer.zoo.enclosures.zip(allShopsAsLists)) {
            for (shop in enclosure.first.shops.zip(enclosure.second)) {
                shop.second.visual =
                    if (shop.first == null) {
                        ImageVisual("Ground_tile_dark.PNG").also {
                            emptyEnclosureSpacesIndices.add(
                                allCardLabels.indexOf(
                                    shop.second
                                )
                            )
                        }
                    } else {
                        ImageVisual(cardImageLoader.getImageForCard(shop.first!!)).also {
                            shop.second.apply {
                                isDraggable = true
                            }
                        }
                    }
            }
        }

        //Refresh Expansions
        for (e in (animalsExp1 + shopsExp1)) {
            e.isVisible = (playerSize == 2 && forPlayer.zoo.expanded <= 1) ||
                    (playerSize > 2 && forPlayer.zoo.expanded == 0)
        }
        for (e in (animalsExp2 + shopsExp2)) {
            e.isVisible = (playerSize == 2 && forPlayer.zoo.expanded == 0)
        }

        (allCardLabels + allRedDotted).forEach {
            if (components.contains(it)) removeComponents(it)
            addComponents(it)
        }

        allCardLabels.forEach {
            it.isDraggable =
                !currentlyScouting
                && !emptyEnclosureSpacesIndices.contains(allCardLabels.indexOf(it))
        }

        barnMap.clear()
        barnLinearLayouts.forEach { it.clear() }
        barnLinearLayouts.first().addAll(forPlayer.zoo.barn.shops.filterNotNull().map { cs ->
            TokenView(
                width = 90,
                height = 90,
                posX = 0,
                posY = 0,
                visual = ImageVisual(cardImageLoader.getImageForCard(cs))
            ).also {
                barnMap[it] = cs
            }
        })

        val cardsInBarn = forPlayer.zoo.barn.animals.filterNotNull().map { ca ->
            TokenView(
                width = 90,
                height = 90,
                posX = 0,
                posY = 0,
                visual = ImageVisual(cardImageLoader.getImageForCard(ca))
            ).also {
                barnMap[it] = ca
            }
        }
        for (i in cardsInBarn.indices){
            barnLinearLayouts[1 + (i % 3)].add(cardsInBarn[i])
        }

        currentPlayerLabel.text = "$currentPlayer"
        balancePlayerNumberLabel.text = "${currentPlayer.balance}"
        balanceBankLabel.text = "The Bank has ${currentGame.bankCoins} coins"

        deckCardsAmountLabel.text = "${currentGame.deck.size} Cards left"
        extraCardsAmountLabel.text = "${currentGame.extraCards.size} Cards left"

    }

    /**
     * Scout Stuff
     */

    /**
     * ATTENTION: Do not call this method from a mouse event. For some reason, calling from a mouse entered
     * event invokes it multiple times and it ruins the functionality of the event.
     */
    private fun moveToFront(vararg components: UIComponent) {
        components.forEach {
            removeComponents(it)
            addComponents(it)
        }
    }

    private val endGameButton = Button(width = 100, height = 40, posX = 0, posY = 1040, text = "End Game!").apply {
        onMouseClicked = {
            rootService.gameService.endGame()
            println("Has Game ended? " + rootService.gameService.gameHasEnded)
            println("EndGameMenuScene should be shown here")
        }
    }

    private val loadGameButton = Button(width = 100, height = 40, posX = 100, posY = 1040, text = "Load Game!").apply {
        onMouseClicked = {
            app.showFileDialog(
                FileDialog(
                    mode = FileDialogMode.OPEN_FILE,
                    title = "Load game"
                )
            ).ifPresent { files ->
                rootService.ioService.loadGameFromFile(files[0].absolutePath)
                refreshAfterStartNewGame()
            }
        }
    }

    init {
        addComponents(endGameButton)
        addComponents(loadGameButton)
    }

    private fun redoTurn(){
        if (turnTodo != null) {
            //println("RedoTurn was not null and was redone.")
            rootService.gameService.turn(turnTodo!!)
            turnTodo = null
            updateTiles(currentPlayer)
        }
    }

    /**
     * Drag  / Drop / Mouseclicked Biggest Functions
     */

    private fun droppedOnDiscardArea() = { dragEvent: DragEvent ->
        val dropped = dragEvent.draggedComponent
        if (!currentlyScouting && currentGame.currentPlayer.balance > 1 && barnMap.containsKey(dropped)) {
            val card = barnMap[dropped]!!
            turnTodo = Turn.MoneyAction(MoneyAction.Discard1Token(card))
            dragEvent.draggedComponent.isVisible = false
            false.also{redoTurn()}
        } else false
    }

    /**
     * Used for purchase 1 tile.
     */

    private fun droppedOnBuyArea() = { dragEvent: DragEvent ->
        val dropped = dragEvent.draggedComponent
        if (currentlyScouting && !currentlyDuringATurn) {
            if (currentGame.currentPlayer.balance > 1 && barnMap.containsKey(dropped)) {
                purchaseSPlayer = currentlyScoutingPlayer!!
                showBoughtCard.visual = dropped.visual
                purchaseChosenCard = barnMap[dropped]!!
                currentlyDuringATurn = true
                updateTiles(currentPlayer)
                true
            } else false
        } else false
    }

    private fun scoutClicked(owner: Button, player: Player) {
        if (player == currentPlayer) {
            enableDraw()
            currentlyScouting = false
            currentlyScoutingPlayer = null
            selectedTruckCards.forEach { it.isDraggable = true}
            refreshPlayerLabels()
        } else {
            disableDraw()
            currentlyScouting = true
            currentlyScoutingPlayer = player
            selectedTruckCards.forEach { it.isDraggable = false }
            refreshPlayerLabels(player)

        }
        playerButtons.forEach { it.isDisabled = it == owner }
        updateTiles(player)
        if (player != currentPlayer) {
            currentPlayerLabel.text = "$player"
            balancePlayerNumberLabel.text = "${player.balance}"
            balanceBankLabel.text = "Spectating this Player"
        }
    }


    private fun playerButtonsMouse(owner: Button, playerIndex: Int, picturePath: String) = { _: MouseEvent ->
        if (!owner.isDisabled) {
            owner.visual = CompoundVisual(
                ImageVisual(picturePath),
                TextVisual("${players[playerIndex]}", Font(size = 24))
            )
        }
    }

    private fun playerMenuArrowClicked(owner: Button) = { _: MouseEvent ->
        if (owner.posY == 1045.0) {
            owner.visual = ImageVisual("04 - Arrow Down.png")
            owner.posY = 966.0

            when (playerSize) {
                2 -> {
                    playerButtons[0].posX = 610.5
                    playerButtons[1].posX = 785.5
                }
                3 -> {
                    playerButtons[0].posX = 523.0
                    playerButtons[1].posX = 698.0
                    playerButtons[2].posX = 872.5
                }
                4 -> {
                    playerButtons[0].posX = 436.3
                    playerButtons[1].posX = 611.0
                    playerButtons[2].posX = 785.0
                    playerButtons[3].posX = 960.0
                }
                5 -> {
                    playerButtons[0].posX = 350.0
                    playerButtons[1].posX = 524.0
                    playerButtons[2].posX = 698.0
                    playerButtons[3].posX = 872.0
                    playerButtons[4].posX = 1046.0
                }
            }
            playerButtons.take(playerSize).forEach { addComponents(it) }
            refreshPlayerLabels(currentlyScoutingPlayer)
        } else if (owner.posY == 966.0) {
            owner.visual = ImageVisual("03 - Arrow Up.png")
            owner.posY = 1045.0
            playerButtons.take(playerSize).forEach { removeComponents(it) }
        }
    }

    private val playerButtonsSet: BidirectionalMap<Player, Button> = BidirectionalMap()
    /**
     * Refreshes player labels hidden by arrow (scout)
     * @param scoutedPlayer leave empty, unless scout is being used.
     */
    private fun refreshPlayerLabels(scoutedPlayer: Player? = null) {
        playerButtons.take(playerSize).forEach { button ->
            if(players.indexOf(currentPlayer) == playerButtons.indexOf(button)) {
                button.visual = CompoundVisual(
                    ImageVisual("images/PlayerLabelTurn.png"),
                    TextVisual("${players[playerButtons.indexOf(button)]}", Font(size = 24))
                )
            }
            if(scoutedPlayer != null && players.indexOf(scoutedPlayer) == playerButtons.indexOf(button)) {
                CompoundVisual(
                    //ImageVisual("images/PlayerLabelHover.png"),
                    TextVisual("${players[playerButtons.indexOf(button)]}", Font(size = 24))
                )
            }
            else {
                CompoundVisual(
                    ImageVisual("images/PlayerLabel.png"),
                    TextVisual("${players[playerButtons.indexOf(button)]}", Font(size = 24))
                )
            }
        }
    }

    private fun onDrawClicked(owner: Button) : ((MouseEvent)->Unit) = { _: MouseEvent ->
        if(!currentlyScouting){
            if (currentGame.deck.isNotEmpty()) {
                if (currentGame.deck.size == 1) {
                    deckLabel.visual = redDiscOnExtraCards.visual
                    redDiscOnExtraCards.visual = Visual.EMPTY
                    removeComponents(drawButton)
                    drawButton = Button(
                        width = deckLabel.width,
                        height = deckLabel.height,
                        posX = extraCardsLabel.posX,
                        posY = extraCardsLabel.posY,
                        font = Font(size = 27,color = Color(50,255,20), fontWeight = Font.FontWeight.BOLD,family = "Pokemon Solid"),
                        visual = Visual.EMPTY
                    ).apply {
                        onMouseEntered = {
                            if (!isDisabled) {
                                tooltipViewer.visual = improvedUI.showTooltipVisual(
                                    this, "Draw 1 tile and place it"
                                )
                            }
                        }
                        onMouseExited = { if (!isDisabled) improvedUI.hideTooltip() }
                        onMouseClicked = onDrawClicked(this)
                    }
                    addComponents(drawButton)

                }
                owner.isVisible = false
                showDrawnCard.isVisible = true
                showDrawnCard.isFocusable = true
                showDrawnCard.visual = ImageVisual(cardImageLoader.getImageForCard(currentGame.deck.first()))
                currentlyDuringATurn = true
                redDottedShowTrucks()
            } else {
                extraCardsLabel.visual = ImageVisual(cardImageLoader.backImage)

                if (currentGame.extraCards.isNotEmpty()) {
                    owner.isVisible = false
                    showEndDrawnCard.isVisible = true
                    showEndDrawnCard.isFocusable = true
                    showEndDrawnCard.visual = ImageVisual(cardImageLoader.getImageForCard(currentGame.extraCards.first()))
                    currentlyDuringATurn = true
                    redDottedShowTrucks()
                }
            }
        }
    }

    private fun droppedOnAnyEnclosureSpace(owner: Area<TokenView>) = { dragEvent: DragEvent ->
        if (dragEvent.draggedComponent != showDrawnCard && selectedTruckCards.indexOf(dragEvent.draggedComponent) == -1) {
            if (emptyEnclosureSpacesIndices.contains(allCardLabels.indexOf(owner))) {
                droppedOnEmptyEnclosureSpace(owner, dragEvent)
            } else {
                droppedOnNotEmptyEnclosureSpace(owner, dragEvent)
            }
        } else false.also{redoTurn()}
    }

    private fun droppedOnEmptyEnclosureSpace(draggedOn: Area<TokenView>, dragEvent: DragEvent): Boolean {
        if (!currentlyScouting && !currentlyDuringATurn) {
            val draggedComp = dragEvent.draggedComponent
            val encIndexDraggedOn = componentToEnclosure(draggedOn)
            val enclosureDraggedOn = currentPlayer.zoo.enclosures[encIndexDraggedOn.first]
            if(componentToEnclosure(draggedOn).first==componentToEnclosure(draggedComp).first) return false
            if (currentGame.currentPlayer.balance < 1) return false
            val draggedIsFromBarn = barnMap.containsKey(draggedComp)
            if(draggedIsFromBarn && barnMap[draggedComp]!! is Card.Animal && allShops.contains(draggedOn)) return false
            if(allShops.contains(draggedOn) && allAnimals.contains(draggedComp)) return false
            val card = if (draggedIsFromBarn) {
                barnMap[draggedComp]!!
            } else if (allShops.contains(draggedOn)) {
                    currentPlayer.zoo.enclosures[componentToEnclosure(draggedComp).first].shops[componentToEnclosure(
                        draggedComp
                    ).second]!!
            } else return false
            if (card is Card.Shop && !allShops.contains(draggedOn)) return false
            if (card is Card.Coin) return false
            val movingToEnclosureWithOtherAnimalType =
                card is Card.Animal && enclosureDraggedOn.animals.filterNotNull()
                    .any { it.animalType != card.animalType }
            if (!draggedIsFromBarn && movingToEnclosureWithOtherAnimalType) return false
            turnTodo = Turn.MoneyAction(MoneyAction.Move1Token(enclosureDraggedOn, card, encIndexDraggedOn.second))
            if (card is Card.Animal) {
                draggedComp.isVisible = false
            }
            if(draggedIsFromBarn && allShops.contains(draggedOn) ){
                draggedComp.visual = ImageVisual(cardImageLoader.getImageForCard(Card.Coin))
                draggedComp.isDisabled = true
                draggedComp.isVisible = false
                draggedComp.isFocusable = false
            }
             if(!draggedIsFromBarn && allShops.contains(draggedOn)){
                 return false

            }else{
                 return true.also{redoTurn()}
            }
        } else if (currentlyScouting && currentlyDuringATurn && showBoughtCard.visual!= Visual.EMPTY) {
            val encIndexDraggedOn = componentToEnclosure(draggedOn)
            val enclosureDraggedOn = currentPlayer.zoo.enclosures[encIndexDraggedOn.first]
            if (purchaseChosenCard is Card.Animal && enclosureDraggedOn != currentPlayer.zoo.barn &&
                enclosureDraggedOn.animals.filterNotNull()
                    .any { it.animalType != (purchaseChosenCard as Card.Animal).animalType }
            ) return false
            turnTodo = Turn.MoneyAction(
                MoneyAction.Purchase1Token(
                    enclosureDraggedOn,
                    purchaseSPlayer,
                    purchaseChosenCard,
                    encIndexDraggedOn.second
                )
            )
            currentlyDuringATurn = false
            currentlyScouting = false
            currentlyScoutingPlayer = null
            showBoughtCard.visual = Visual.EMPTY
            return false.also{redoTurn()}
        }
        return false
    }

    private fun droppedOnNotEmptyEnclosureSpace(owner: Area<TokenView>, dragEvent: DragEvent): Boolean {
        val draggedLabel = dragEvent.draggedComponent
        val encIndexDraggedOn = componentToEnclosure(owner)
        val enclosureDraggedOn = currentPlayer.zoo.enclosures[encIndexDraggedOn.first]
        val encIndexDraggedFrom = componentToEnclosure(draggedLabel)
        val enclosureDraggedFrom = currentPlayer.zoo.enclosures[encIndexDraggedFrom.first]
        if (enclosureDraggedOn == enclosureDraggedFrom) return false
        if (currentGame.currentPlayer.balance < 1) return false
        if (allShops.contains(draggedLabel) || allShops.contains(owner)) return false
        if (currentPlayer.zoo.enclosures[componentToEnclosure(draggedLabel).first].animals[componentToEnclosure(
                draggedLabel
            ).second] == null
        ) return false
        var cardDragged: Card.Animal =
            currentPlayer.zoo.enclosures[componentToEnclosure(draggedLabel).first].animals[componentToEnclosure(
                draggedLabel
            ).second]!!
        if (barnMap.containsKey(draggedLabel)) {
            if (barnMap[draggedLabel]!! is Card.Animal) {
                cardDragged = barnMap[draggedLabel]!! as Card.Animal
            } else return false
        }
        if(enclosureDraggedOn.animals[encIndexDraggedOn.second]==null) {return false}
        val cardDraggedOn = enclosureDraggedOn.animals[encIndexDraggedOn.second]!!
        if (enclosureDraggedOn.animals.filterNotNull()
                .filter { cardDraggedOn.animalType == it.animalType }.size > enclosureDraggedFrom.animalCapacity
            || enclosureDraggedFrom.animals.filterNotNull()
                .filter { cardDragged.animalType == it.animalType }.size > enclosureDraggedOn.animalCapacity
        ) return false
        if (cardDragged.animalType == cardDraggedOn.animalType) return false
        var male = Card.Animal(cardDragged.animalType, Maturity.Male, true)
        var female = Card.Animal(cardDragged.animalType, Maturity.Male, true)

        /*
        if(enclosureDraggedFrom.isBarn && enclosureDraggedFrom.animals.contains(male)
                    && enclosureDraggedFrom.animals.contains(female)){
            if(enclosureDraggedFrom.animals.filterNotNull()
                    .filter { cardDragged.animalType == it.animalType }.size-1 < enclosureDraggedOn.animalCapacity) {
                enclosureDraggedOn.add(Card.Animal(cardDragged.animalType, Maturity.OffSpring, false))
                male.isFertile = false
                female.isFertile = false
            }
            else {
                enclosureDraggedFrom.animals.add(Card.Animal(cardDragged.animalType, Maturity.OffSpring, false))
                male.isFertile = false
                female.isFertile = false
            }
        }
        */
        turnTodo = Turn.MoneyAction(
            MoneyAction.Swap2Types(
                mapOf(
                    enclosureDraggedOn to cardDraggedOn.animalType,
                    enclosureDraggedFrom to cardDragged.animalType
                )
            )
        )
        println("1")
        return false
    }

    private fun droppedOnAnyEnclosureSpaceRedDotted(owner: Area<TokenView>) = { dragEvent: DragEvent ->
        if (dragEvent.draggedComponent != showDrawnCard && selectedTruckCards.indexOf(dragEvent.draggedComponent) > -1) {
            when (val card = selectedTruck.cards[selectedTruckCards.indexOf(dragEvent.draggedComponent) % 3]) {
                null -> false
                else -> {
                    val cardPatch = allCardLabels[allRedDotted.indexOf(owner)]
                    val temp = componentToEnclosure(cardPatch)
                    val enclosureIndex = temp.first
                    val index = temp.second
                    if ((card is Card.Shop && !allShops.contains(cardPatch) || card is Card.Animal && !allAnimals.contains(
                            cardPatch
                        ))
                    ) {
                        false
                    } else {
                        val enclosure = currentPlayer.zoo.enclosures[enclosureIndex]
                        if ((card is Card.Animal && (
                                    takingTruckMap.filter { it.second == enclosure && it.first is Card.Animal }
                                        .any { (it.first as Card.Animal).animalType != card.animalType }
                                            || enclosure.animals.filterNotNull()
                                        .any { it.animalType != card.animalType }
                                    ))
                        ) {
                            false
                        } else {
                            if (card is Card.Shop || card is Card.Animal) {
                                takingTruckMap.add(Triple(card, enclosure, index))
                                cardPatch.visual = dragEvent.draggedComponent.visual
                                allRedDotted[allCardLabels.indexOf(cardPatch)].isVisible = false
                                if (takingTruckMap.size == selectedTruck.cards.filterNotNull()
                                        .filterNot { it is Card.Coin }.size
                                ) {
                                    val coinMap =
                                        List<Triple<Card, Enclosure?, Int>>(
                                            selectedTruck.cards.filterIsInstance<Card.Coin>().size
                                        ) {
                                            Triple(
                                                Card.Coin,
                                                currentPlayer.zoo.enclosures[1],
                                                0
                                            )
                                        }
                                    takingTruckMap.addAll(coinMap)
                                    rootService.gameService.turn(Turn.TakeTruck(selectedTruck, takingTruckMap))
                                    currentlyDuringATurn = false
                                    takingTruckMap = mutableListOf()
                                }
                            }
                            card !is Card.Coin
                        }
                    }
                }
            }
        } else {
            false
        }
    }

    private fun saveButtonClicked() = { _: MouseEvent ->
        app.showFileDialog(
            FileDialog(
                mode = FileDialogMode.SAVE_FILE,
                title = "Save game"
            )
        ).ifPresent { files ->
            println("Written ${files[0]}")
            // Only accept txt files, otherwise throw an error message
            if (files[0].extension == "txt") {
                // Writing works okay
                // TODO use selected directory?
                rootService.ioService.saveGameToFile(files[0].name)
            } else {
                app.showDialog(
                    Dialog(
                        dialogType = DialogType.ERROR,
                        title = "Save game failed :(",
                        header = "Invalid file extension!",
                        message = "Please write a .txt file! It's not that hard."
                    )
                )
            }
        }
    }

    private fun droppedFromEnclosureToBarn(draggedOn: LinearLayout<TokenView>, dragEvent: DragEvent): Boolean {
        val draggedComp = dragEvent.draggedComponent
        val encIndexDraggedOn = componentToEnclosure(draggedOn)
        val enclosureDraggedOn = currentPlayer.zoo.enclosures[encIndexDraggedOn.first]
        val encIndexDraggedFrom = componentToEnclosure(draggedComp)
        val enclosureDraggedFrom = currentPlayer.zoo.enclosures[encIndexDraggedFrom.first]
        if (enclosureDraggedOn == enclosureDraggedFrom) return false
        if (currentGame.currentPlayer.balance < 1) return false
        if (allAnimals.contains(draggedComp)) return false
        val card =
            currentPlayer.zoo.enclosures[componentToEnclosure(draggedComp).first].shops[componentToEnclosure(draggedComp).second]!!
        turnTodo = Turn.MoneyAction(MoneyAction.Move1Token(currentPlayer.zoo.barn, card, 0))
        return false
    }

    private fun droppedOnBarn(owner: LinearLayout<TokenView>) = { dragEvent: DragEvent ->
        if (dragEvent.draggedComponent != showDrawnCard && selectedTruckCards.indexOf(dragEvent.draggedComponent) > -1) {
            when (val card = selectedTruck.cards[selectedTruckCards.indexOf(dragEvent.draggedComponent) % 3]) {
                null -> {}
                else -> {
                    val temp = componentToEnclosure(owner)
                    val enclosureIndex = temp.first
                    val encIndex = if (card is Card.Animal) {
                        currentPlayer.zoo.barn.animals.indexOf(currentPlayer.zoo.barn.animals.filter { it == null }[0])
                    } else {
                        currentPlayer.zoo.barn.shops.indexOf(currentPlayer.zoo.barn.shops.filter { it == null }[0])
                    }
                    val index = alreadyDroppedInBarn + encIndex //temp.second
                    val enclosure = currentPlayer.zoo.enclosures[enclosureIndex]
                    if (card is Card.Shop || card is Card.Animal) {
                        alreadyDroppedInBarn++
                        takingTruckMap.add(Triple(card, enclosure, index))
                        owner.add(
                            TokenView(
                                width = 90,
                                height = 90,
                                posX = 0,
                                posY = 0,
                                visual = ImageVisual(cardImageLoader.getImageForCard(card))
                            )
                        )
                        dragEvent.draggedComponent.also{
                            it.isVisible = false
                            it.isDisabled = true
                            it.isFocusable = false
                        }
                        if (takingTruckMap.size == selectedTruck.cards.filterNotNull()
                                .filterNot { it is Card.Coin }.size
                        ) {
                            val coinMap =
                                List<Triple<Card, Enclosure?, Int>>(
                                    selectedTruck.cards.filterIsInstance<Card.Coin>().size
                                ) {
                                    Triple(
                                        Card.Coin,
                                        currentPlayer.zoo.enclosures[1],
                                        0
                                    )
                                }
                            takingTruckMap.addAll(coinMap)
                            rootService.gameService.turn(Turn.TakeTruck(selectedTruck, takingTruckMap))
                            alreadyDroppedInBarn = 0
                            currentlyDuringATurn = false
                            takingTruckMap = mutableListOf()
                        }
                    }
                    (card !is Card.Coin).also {redoTurn()}
                }
            }
        } else if (currentlyScouting && currentlyDuringATurn && showBoughtCard.visual!= Visual.EMPTY) {
            val enclosureDraggedOn = currentPlayer.zoo.barn
            val encIndex = if (purchaseChosenCard is Card.Animal) {
                currentPlayer.zoo.barn.animals.indexOf(currentPlayer.zoo.barn.animals.filter { it == null }[0])
            } else {
                currentPlayer.zoo.barn.shops.indexOf(currentPlayer.zoo.barn.shops.filter { it == null }[0])
            }
            turnTodo = Turn.MoneyAction(
                MoneyAction.Purchase1Token(
                    enclosureDraggedOn,
                    purchaseSPlayer,
                    purchaseChosenCard,
                    encIndex
                )
            )
            currentlyDuringATurn = false
            currentlyScouting = false
            currentlyScoutingPlayer = null
            showBoughtCard.visual = Visual.EMPTY
            false.also {redoTurn()}

        } else if(dragEvent.draggedComponent != showDrawnCard &&
            selectedTruckCards.indexOf(dragEvent.draggedComponent)== -1) {
            droppedFromEnclosureToBarn(owner, dragEvent)
        }
        false
    }

    private fun droppedOnTruck(owner: Area<TokenView>) = { dragEvent: DragEvent ->
        if (dragEvent.draggedComponent == showDrawnCard || dragEvent.draggedComponent == showEndDrawnCard) {
            when (dragEvent.draggedComponent) {
                is TokenView -> {
                    val truckIndex = (trucksCardLabels.indexOf(owner) / 3)
                    val truck = currentGame.trucks[truckIndex]
                    val index = trucksCardLabels.indexOf(owner) % 3

                    if (dragEvent.draggedComponent == showEndDrawnCard) {
                        showEndDrawnCard.visual = Visual.EMPTY
                        showEndDrawnCard.isVisible = false
                        showEndDrawnCard.isFocusable = false
                    }

                    val twoPlayerDontsList = listOf(Pair(1, 2), Pair(2, 1), Pair(2, 2))
                    val twoPlayerDont = (playerSize == 2 && twoPlayerDontsList.contains(Pair(truckIndex, index)))

                    if (truck.cards[index] == null && !twoPlayerDont) {
                        rootService.gameService.turn(Turn.Draw(index = index, truck = truck))
                        drawButton.isVisible = true
                        trucksRedDottedCardsLabels.forEach { it.isVisible = false }
                        showDrawnCard.visual = Visual.EMPTY
                        showDrawnCard.isVisible = false
                        showDrawnCard.isFocusable = false
                        true
                    }
                    false
                }
                else -> false
            }
        } else {
            false
        }
    }

    private fun takeTruckClicked(owner: Label) {
        if (!currentlyDuringATurn && !currentlyScouting) {
            val selectedTruckIndex = trucksWholeRedDottedLabels.indexOf(owner)
            selectedTruck = currentGame.trucks[selectedTruckIndex]
            val selectedTruckLabel = truckLabels[selectedTruckIndex]
            val coinMap = List<Triple<Card, Enclosure?, Int>>(selectedTruck.cards.filterIsInstance<Card.Coin>().size) {
                Triple(
                    Card.Coin,
                    currentPlayer.zoo.enclosures[1],
                    0
                )
            }
            if (selectedTruck.cards.filterNotNull().none { it !is Card.Coin }) {
                // When Truck just has Coins
                rootService.gameService.turn(
                    Turn.TakeTruck(
                        truck = selectedTruck,
                        map = coinMap
                    )
                )
            } else {
                if(!currentlyScouting){
                truckLabels.forEach { it.isVisible = false }
                trucksCardLabels.forEach { it.isVisible = false }
                alreadyTakenTrucks.add(trucksWholeRedDottedLabels.indexOf(owner))
                selectedTruckCards = generatedTrucks[trucksWholeRedDottedLabels.indexOf(owner)].third
                selectedTruckCards.forEach { it.isVisible = true; it.isDraggable=true }
                selectedTruckLabel.isVisible = true
                disableDraw()
                currentlyDuringATurn = true
                allRedDotted.forEach { it.isVisible = emptyEnclosureSpacesIndices.contains(allRedDotted.indexOf(it)) }
                (redDottedAnimalsExp1 + redDottedShopsExp1).forEach {
                    it.isVisible = currentPlayer.zoo.enclosures.size > 4
                }
                (redDottedAnimalsExp2 + redDottedShopsExp2).forEach {
                    it.isVisible = currentPlayer.zoo.enclosures.size > 5
                }
            }
            }
        }
    }

    /**
     * Related to money actions. Only information.
     */

    private val moneyActionsButton = Button(
        posX = 1400, posY = 73, height = 76, width = 76,
        visual = ImageVisual("images/MoneyActions.png")
    ).apply {
        onMouseEntered = {
            visual = ImageVisual("images/MoneyActionsHover.png")
            tooltipViewer.visual = improvedUI.showTooltipVisual(this, "Show / hide money actions")
        }
        onMouseExited = {
            visual = ImageVisual("images/MoneyActions.png")
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            moneyActionArrow.visual = if (!maMove1Tile.isVisible) {
                ImageVisual("03 - Arrow Up.png")
            } else {
                ImageVisual("04 - Arrow Down.png")
            }
            listOf(
                maMove1Tile, maExchange2TypesOfTiles, maDiscard1Tile, maPurchase1Tile, maExpandZoo
            ).forEach {
                it.isVisible = !it.isVisible
                it.isFocusable = !it.isFocusable
            }
        }
    }

    /* Just a nice little arrow indicator */
    private val moneyActionArrow: Label = Label(
        posX = moneyActionsButton.posX + 50, posY = moneyActionsButton.posY + 50, height = 32, width = 32,
        visual = ImageVisual("04 - Arrow Down.png")
    ).apply { isFocusable = false }

    private val maMove1Tile: Button = Button(
        posX = moneyActionsButton.posX, posY = moneyActionsButton.posY + 80,
        width = moneyActionsButton.width, height = moneyActionsButton.height,
        visual = ImageVisual("images/Move1Tile.png")
    ).apply {
        isVisible = false
        isFocusable = false
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(this, "Move 1 tile")
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            app.showDialog(
                Dialog(
                    dialogType = DialogType.INFORMATION,
                    title = "Money action - 1 coin",
                    header = "Move 1 tile, costs 1 coin:",
                    message = "Drag a tile and drop it over a free spot."
                )
            )
        }
    }

    private val maExchange2TypesOfTiles = Button(
        posX = maMove1Tile.posX, posY = maMove1Tile.posY + 80,
        width = maMove1Tile.width, height = maMove1Tile.height,
        visual = ImageVisual("images/Swap2Types.png")
    ).apply {
        isVisible = false
        isFocusable = false
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(this, "Exchange 2 types of tiles")
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            app.showDialog(
                Dialog(
                    dialogType = DialogType.INFORMATION,
                    title = "Money action - 1 coin",
                    header = "Exchange 2 types of tiles, costs 1 coin:",
                    message = "Drag a tile and drop it over a tile of a different kind."
                )
            )
        }
    }

    private val maPurchase1Tile = Button(
        posX = maExchange2TypesOfTiles.posX, posY = maExchange2TypesOfTiles.posY + 80,
        width = maExchange2TypesOfTiles.width, height = maExchange2TypesOfTiles.height,
        visual = ImageVisual("images/Purchase1Tile.png")
    ).apply {
        isVisible = false
        isFocusable = false
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(this, "Purchase 1 tile")
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            app.showDialog(
                Dialog(
                    dialogType = DialogType.INFORMATION,
                    title = "Money action - 2 coins",
                    header = "Purchase 1 tile, costs 2 coins (1 coin for seller):",
                    message = "Head to the board of a player and drag a tile into the shopping cart."
                )
            )
        }
    }

    private val maDiscard1Tile = Button(
        posX = maPurchase1Tile.posX, posY = maPurchase1Tile.posY + 80,
        width = maPurchase1Tile.width, height = maPurchase1Tile.height,
        visual = ImageVisual("images/Discard1Tile.png")
    ).apply {
        isVisible = false
        isFocusable = false
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(this, "Discard 1 tile")
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            app.showDialog(
                Dialog(
                    dialogType = DialogType.INFORMATION,
                    title = "Money action - 2 coins",
                    header = "Discard 1 tile, costs 2 coins:",
                    message = "Drag a tile and drop it on bin."
                )
            )
        }
    }

    private val maExpandZoo = Button(
        posX = maDiscard1Tile.posX, posY = maDiscard1Tile.posY + 80,
        width = maDiscard1Tile.width, height = maDiscard1Tile.height,
        visual = ImageVisual("images/Expand.png")
    ).apply {
        isVisible = false
        isFocusable = false
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(this, "Expand your zoo")
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            app.showDialog(
                Dialog(
                    dialogType = DialogType.INFORMATION,
                    title = "Money action - 3 coins",
                    header = "Expand zoo, costs 3 coins:",
                    message = "Click on the expansion board on the left to expand."
                )
            )
        }
    }

    /*
    /**
     * This function is used to confirm an action before proceeding.
     * @return true if user clicks yes, otherwise false.
     * @param title is the title for dialog window.
     * @param header is the header for dialog window.
     * @param message is the message for dialog window.
     */
    private fun confirmAction(
        title: String = "Confirm action",
        header: String = "Are you sure you want to do this?",
        message: String = "",
    )
            : Boolean {
        var result = false
        app.showDialog(
            Dialog(
                dialogType = DialogType.CONFIRMATION,
                title = title,
                header = header,
                message = message
            )
        ).ifPresent {
            result = it == ButtonType.YES
        }
        println("Player selected $result")
        return result
    }
     */

    init {
        addComponents(
            moneyActionsButton, moneyActionArrow,
            maMove1Tile, maExchange2TypesOfTiles, maDiscard1Tile, maPurchase1Tile, maExpandZoo
        )
    }
}