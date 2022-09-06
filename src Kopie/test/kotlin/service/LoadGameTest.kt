package service
import entity.Player
import kotlin.test.*
import entity.*

/**
 * Test the [GameService]
 */
class LoadGameTest {
    private lateinit var rootService: RootService

    /**
     * Tests the Load and storegamemethod
     */
    @Test
    fun LoadAndStoreGameTest(){
        rootService = RootService()
        rootService.gameMaster = GameMaster(ZoolorettoGame(listOf("p1", "p2", "p3").map{ Player.HumanPlayer(it) }))
        rootService.gameMaster.currentGame.currentPlayer.balance = 100

    }



}