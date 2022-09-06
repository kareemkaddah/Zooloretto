package entity

import service.RootService
import service.Turn
import kotlin.test.*

/**
 * Tests [GameMaster]
 */
class GameMasterTest {

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
     * Tests appendGame
     */
    @Test
    fun appendGame(){
        assertEquals(1,rootService.gameMaster.turns.size)
        rootService.gameMaster.appendGame()
        assertEquals(2,rootService.gameMaster.turns.size)
    }

    /**
     * Tests redo
     */
    @Test
    fun undo(){
        assertEquals(0,rootService.gameMaster.currentGameIndex)
        rootService.gameMaster.appendGame()
        rootService.gameMaster.undo()
        assertEquals(0,rootService.gameMaster.currentGameIndex)
    }

    /**
     * Tests redo
     */
    @Test
    fun redo(){
        assertEquals(0,rootService.gameMaster.turns.indexOf(rootService.gameMaster.currentGame))

        val truck = rootService.gameMaster.currentGame.trucks[1]
        val index = 2
        rootService.gameService.turn(Turn.Draw(truck,index))

        rootService.gameMaster.undo()
        rootService.gameMaster.redo()
        assertEquals(1,rootService.gameMaster.currentGameIndex)
    }

}