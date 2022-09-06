package view

import entity.Player
import service.RootService
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color
import kotlin.system.exitProcess

/**
 * Scene that appears after game is over.
 */
class EndGameMenuScene(private val rootService: RootService) :
    MenuScene(width = 1920, height = 1080, background = ImageVisual("images/Pokéretto_Menu_Scene.png")), Refreshable {

    /**
     * Global font.
     */
    private val globalFont: Font = Font(
        size = 36, color = Color(252, 215, 3),
        family = "Pokemon Solid"
    )

    private val improvedUI: ImprovedUI = ImprovedUI()

    private val tooltipViewer: Label = Label(
        height = 50, visual = CompoundVisual(ImageVisual("images/Bubble.png"))
    ).apply {
        isVisible = false
        isFocusable = false
    }

    private val scoreBg = Label(
        width = 821,
        height = 725,
        posX = 520,
        posY = 300,
        visual = ImageVisual("images/EndGameScores.png")
    ).apply { opacity = 0.95 }

    val exitButton = Button(
        width = 200, height = 172, posX = 170, posY = 860,
        alignment = Alignment.CENTER,
        visual = ImageVisual("images/Snorlax_Quit_Button_New.png")
    ).apply {
        if (!this.isDisabled) {
            onMouseClicked = { exitProcess(0) }
        }
    }

    /**
     * Show the local highscores in a list
     */
    fun showLocalHighScore() {
        val game = rootService.gameMaster.currentGame
        val playerScoreList = mutableListOf<Player>()
        for (player in game.players) {
            playerScoreList.add(player)
        }

        val scoreList: List<Player> = playerScoreList.sortedWith(compareBy({ it.score() }, { it.balance })).asReversed()

        val medal = Label(
            640,
            605,
            78,
            78,
            visual = ImageVisual("images/FIRST PRIZE.png")
        )

        val winner = Label(
            732,
            599,
            550,
            78,
            "1. " + scoreList[0].getPlayerName() + ": " + scoreList[0].score() + " P. Balance: " + scoreList[0].balance,
            font = globalFont,
            alignment = Alignment.CENTER_LEFT
        )
        val second = Label(
            732,
            652,
            550,
            78,
            "2. " + scoreList[1].getPlayerName() + ": " + scoreList[1].score() + " P. Balance: " + scoreList[1].balance,
            font = globalFont,
            alignment = Alignment.CENTER_LEFT
        )

        addComponents(winner, second, medal)

        if (scoreList.size == 3) {
            val third = Label(
                732,
                705,
                550,
                78,
                "3. " + scoreList[2].getPlayerName() + ": " + scoreList[2].score() + " P. Balance: " + scoreList[2].balance,
                font = globalFont,
                alignment = Alignment.CENTER_LEFT
            )
            addComponents(third)
        }
        if (scoreList.size == 4) {
            val third = Label(
                732,
                705,
                550,
                78,
                "3. " + scoreList[2].getPlayerName() + ": " + scoreList[2].score() + " P. Balance: " + scoreList[2].balance,
                font = globalFont,
                alignment = Alignment.CENTER_LEFT
            )
            val fourth = Label(
                732,
                758,
                550,
                78,
                "4. " + scoreList[3].getPlayerName() + ": " + scoreList[3].score() + " P. Balance: " + scoreList[3].balance,
                font = globalFont,
                alignment = Alignment.CENTER_LEFT
            )
            addComponents(third, fourth)
        }
        if (scoreList.size == 5) {
            val third = Label(
                732,
                705,
                550,
                78,
                "3. " + scoreList[2].getPlayerName() + ": " + scoreList[2].score() + " P. Balance: " + scoreList[2].balance,
                font = globalFont,
                alignment = Alignment.CENTER_LEFT
            )
            val fourth = Label(
                732,
                758,
                550,
                78,
                "4. " + scoreList[3].getPlayerName() + ": " + scoreList[3].score() + " P. Balance: " + scoreList[3].balance,
                font = globalFont,
                alignment = Alignment.CENTER_LEFT
            )
            val fifth = Label(
                732,
                811,
                550,
                78,
                "5. " + scoreList[4].getPlayerName() + ": " + scoreList[4].score() + " P. Balance: " + scoreList[4].balance,
                font = globalFont,
                alignment = Alignment.CENTER_LEFT
            )
            addComponents(third, fourth, fifth)
        }
    }

    private val globalScoresButton = Button(
        width = 170, height = 172, posX = 1550, posY = 842,
        alignment = Alignment.CENTER,
        visual = ImageVisual("PODIUM.png")
    ).apply {
        var globalScoresHidden = true
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(
                component = this,
                message = "Show / hide Global Highscores"
            )
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            if(globalScoresHidden) {
                showGlobalHighscores()

            } else {
                hideGlobalHighscores()
            }
            globalScoresHidden = !globalScoresHidden
        }
    }

    /**
     * Just for referencing, in order to enable add and remove component.
     */
    private var globalHighscoresTable = TableView<Player>()

    private fun showGlobalHighscores() {
        val players = rootService.gameService.highscorePlayerList
        val table: TableView<Player> = TableView(
            posX = scoreBg.posX + 180, posY = scoreBg.posY + 200, width = scoreBg.width - 300, height = scoreBg.height - 210,
            items = players,
            columns = listOf(
                TableColumn(
                    title = "Player:",
                    width = 300,
                    font = globalFont,
                    formatFunction = { player -> "${players.indexOf(player) + 1}. $player" }
                ), TableColumn(
                    title = "Highscore:",
                    width = 200,
                    font = globalFont,
                    formatFunction = { player -> "${player.highscore}" }
                )),
            visual = Visual.EMPTY,
            selectionMode = SelectionMode.MULTIPLE,
            selectionBackground = ColorVisual(255,255,255, 100)
        ).apply {
            this.componentStyle = "-fx-"
        }
        globalHighscoresTable = table
        removeComponents(scoreBg)
        addComponents(scoreBg, globalHighscoresTable)
    }

    private fun hideGlobalHighscores() {
        removeComponents(globalHighscoresTable)
        showLocalHighScore()
    }

    /*private val startButton = Button(
        width = 200, height = 172, posX = 1550, posY = 842,
        alignment = Alignment.CENTER,
        visual = ImageVisual("images/Pikachu_Start_Button_White.png")).apply {
        onMouseEntered = {
            tooltipViewer.visual = improvedUI.showTooltipVisual(
                component = this,
                message = "Start new game!"
            )
        }
        onMouseExited = {
            improvedUI.hideTooltip()
        }
    }
   */
    init {
        addComponents(exitButton, scoreBg, globalScoresButton, tooltipViewer)
        rootService.gameService.saveHighsores()
        rootService.gameService.loadHighsores()
        improvedUI.tooltipViewer = tooltipViewer
    }

}