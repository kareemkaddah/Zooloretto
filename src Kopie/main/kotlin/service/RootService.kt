package service

import entity.GameMaster
import entity.Player
import entity.ZoolorettoGame
import view.Refreshable

/**
 * Connects Entity and Service layer
 */
class RootService {
    var gameMaster: GameMaster = GameMaster(currentGame=ZoolorettoGame(mutableListOf(Player.HumanPlayer(""))))
    var ioService: IOService = IOService(this)
    var gameService: GameService = GameService(this)
    var playerActionService: PlayerActionService = PlayerActionService()
    var kiServiceV2: KIServiceV2 = KIServiceV2(this)

    /**
     * Adds the provided [newRefreshable] to all services connected
     * to this root service
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }

}