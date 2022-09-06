package service
import entity.Player
import kotlin.test.*
import entity.*

/**
 * Test the [GameService]
 */
class GameServiceTest {
    private lateinit var rootService: RootService
    /**
     * Creates a dummy Game before every test
     */
    @BeforeTest
    fun makeDummyGame() {
        rootService = RootService()
        rootService.gameMaster = GameMaster(ZoolorettoGame(listOf("p1", "p2", "p3").map{ Player.HumanPlayer(it) }))
        rootService.gameMaster.currentGame.currentPlayer.balance = 100
    }
    /**
     * Tests the LoadHighscore method
     */
    @Test
    fun startLoadHighscoreTest(){
        val players = listOf("p1", "p2", "p3").map{ Player.HumanPlayer(it) }
        rootService = RootService().apply { gameService.startNewGame(players) }
        rootService.gameService.loadHighsores()
        rootService.gameService.saveHighsores()
        rootService.ioService.saveHighscorePlayerToFile("Highscores.scores",players)
        var readPlayers= rootService.ioService.loadHighscorePlayer("Highscores.score")
        assertNotEquals(readPlayers, emptyList<Player>())
        rootService.ioService.saveGameToFile("AA")
        rootService.ioService.loadGameFromFile("AA")
        assertNotNull(rootService.gameMaster.currentGame)
        assertNotNull(rootService.gameMaster.currentGameIndex)
        assertNotNull(rootService.gameMaster.turns)
        assertNotNull(rootService.gameService.gameHasEnded)
        assertNotNull(rootService.gameService.highscorePlayerList)
    }

    /**
     * Tests the startNewGame method
     */
    @Test
    fun startNewGameTest(){
        val players = listOf("p1", "p2", "p3").map{ Player.HumanPlayer(it) }
        rootService = RootService().apply { gameService.startNewGame(players) }
        assertEquals(ZoolorettoGame(players),rootService.gameMaster.currentGame)
    }

    /**
     * Tests player swapping after turn
     */
    @Test
    fun playerSwapTest(){
        assertEquals("p1",rootService.gameMaster.currentGame.currentPlayer.toString())
        rootService.gameService.turn(Turn.Draw(rootService.gameMaster.currentGame.trucks[0],0))
        assertEquals("p2",rootService.gameMaster.currentGame.currentPlayer.toString())
        rootService.gameService.turn(Turn.Draw(rootService.gameMaster.currentGame.trucks[0],1))
        assertEquals("p3",rootService.gameMaster.currentGame.currentPlayer.toString())
        rootService.gameService.turn(Turn.Draw(rootService.gameMaster.currentGame.trucks[0],2))
        assertEquals("p1",rootService.gameMaster.currentGame.currentPlayer.toString())
    }


}