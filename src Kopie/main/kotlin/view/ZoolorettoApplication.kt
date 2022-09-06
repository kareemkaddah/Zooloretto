package view

import service.*
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.visual.ImageVisual
import java.io.File
import java.io.FileNotFoundException

/**
 * This is the application that creates a window on the presentation layer. It allows interaction, and manages
 * different scenes. For visualizing elements, we use two types of scenes, GameScene and MenuScene.
 * Scenes are the layer which allows user interaction.
 */
class ZoolorettoApplication: BoardGameApplication("Pokéretto! Greatest board game of all time!"), Refreshable {
    private val rootService = RootService()



    init{
        this.isFullScreen = true
        /* Using external font */
        //val resource = UIComponent::class.java.getResource("/fonts/Pokemon Solid.ttf") ?: throw FileNotFoundException()
        //val fontFile = File(resource.toURI())
        //loadFont(fontFile)
    }

    /**
     * In-game scene.
     */

    private val gameScene = ZoolorettoGameScene(this, rootService)




    /**
     * Lobby scene.
     */
    private val newGameMenuScene = NewGameMenuScene(this, rootService).apply {
        this.exitButton.onMouseClicked = {
            exit()
        }
    }

    /**
     * Leaderboard.
     */

    private val gameFinishedMenuScene = EndGameMenuScene(rootService)



    init {
        this.icon = ImageVisual("images/AppIcon.png")
        /* Make sure all scenes are accessible in order to be refreshed when needed */
        rootService.addRefreshables(
            this,
            gameScene,
            gameFinishedMenuScene,
            newGameMenuScene
        )
        this.showGameScene(gameScene)
        this.showMenuScene(newGameMenuScene, 500)
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
    }

    override fun refreshAfterGameEnd() {
        this.showMenuScene(gameFinishedMenuScene,500)
        gameFinishedMenuScene.showLocalHighScore()
    }
}