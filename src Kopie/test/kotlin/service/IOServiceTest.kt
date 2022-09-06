package service
import entity.Player
import kotlin.test.*
import entity.*

/**
 * Test the [GameService]
 */
class IOServiceTest {

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
     * Tests loadGame and saveGame
     */
    @Test
    fun loadAndSaveGameToFile(){
        val turns = rootService.gameMaster.turns
        rootService.ioService.saveGameToFile("test")
        rootService.ioService.loadGameFromFile("test")
        val loadedTurns = rootService.gameMaster.turns
        assertEquals(turns,loadedTurns)
    }

}