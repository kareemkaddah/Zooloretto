package service

import entity.*
import java.io.File

/**
 * This class manages the start of a new game and the turn.
 *
 * @param rootService current rootserives
 */
class GameService(var rootService: RootService) : AbstractRefreshingService() {

    var gameHasEnded: Boolean = false
    var highscorePlayerList = rootService.ioService.loadHighscorePlayer("Highscores.score")
    /**
     Loads the Highscores
     */
    fun loadHighsores():List<Player>{
        this.highscorePlayerList= rootService.ioService.loadHighscorePlayer("Highscores.score")
        return  rootService.ioService.loadHighscorePlayer("Highscores.score")
     }

    /**
     Saves the Highscores
     */
    fun saveHighsores(){
        rootService.gameMaster.currentGame.players.forEach{e->e.highscore=e.score()}
        highscorePlayerList = highscorePlayerList + rootService.gameMaster.currentGame.players.filter { !it.hasGottenHint }
        val highestScores= highscorePlayerList.sortedBy { e->e.highscore }.take(20)
        rootService.ioService.saveHighscorePlayerToFile("Highscores.score", highestScores)
    }

    /**
     * Starts a new Game of Zooloretto.
     *
     * @param players List of players for the nw game
     */
    fun startNewGame(players: List<Player>, loadFromFile : File? = null) {
        rootService.gameMaster = GameMaster(ZoolorettoGame(players,loadFromFile=loadFromFile))
        rootService.gameMaster.currentGame.players.forEach {
            it.zoo = Zoo(rootService.gameMaster.currentGame.players.size)
        }
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Validates a spcific turn
     *
     * @param t turn to validate
     * @param state on which the turn should be executed
     */
    private fun validateTurn(t: Turn, state: ZoolorettoGame? = null) {
        val game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        when (t) {
            is Turn.Draw -> validatedraw(t)
            is Turn.MoneyAction -> validateMoneyAction(t, game)
            is Turn.TakeTruck -> validateTaketruck(t, game)
        }
    }

    /**
     * Validates Turn.draw
     */
    private fun validatedraw(t: Turn.Draw) {
        //var game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (t.truck.cards.filterNotNull().size == t.truck.capacity) {
            throw UnsupportedOperationException("Truck is already full")
        }
    }

    /**
     * Validates a Moneyaction
     */
    private fun validateMoneyAction(t: Turn.MoneyAction, state: ZoolorettoGame? = null) {
        val game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (game.currentPlayer.balance < rootService.playerActionService.moneyActionCost(t.m)) {
            throw UnsupportedOperationException("Player needs more Money for this MoneyAction")
        }
        when (val m = t.m) {
            is MoneyAction.Move1Token -> validateMove1token(m, game)
            is MoneyAction.Swap2Types -> validateSwap2types(m)
            is MoneyAction.Purchase1Token -> {}
            is MoneyAction.Discard1Token -> validateDiscard1token(m, game)
            is MoneyAction.ExpandZoo -> validateExpandzoo(game)
            else -> {}
        }
    }

    /**
     * Validates Moneyaction.Move1token
     */
    private fun validateMove1token(m: MoneyAction.Move1Token, state: ZoolorettoGame? = null) {
        val game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (m.chosenCard is Card.Animal && !game.currentPlayer.zoo.barn.animals.contains(m.chosenCard)) {
            throw UnsupportedOperationException("This animal card is not coming from the barn")
        }
        if (game.currentPlayer.zoo.enclosures.sumOf { it.animals.size + it.shops.size } == 0) {
            throw UnsupportedOperationException("There are no cards to move")
        }
        if (!m.moveLocation.isBarn) {
            if (m.chosenCard is Card.Animal) {
                if (m.moveLocation.animals.size > 0 && m.moveLocation.animals.filterNotNull()
                        .any { it.animalType != m.chosenCard.animalType }
                ) {
                    throw UnsupportedOperationException("Cant move Card: different Animal types.")
                }
            }
        }
    }

    /**
     * Validates Moneyaction.Swap2types
     */
    private fun validateSwap2types(m: MoneyAction.Swap2Types) {
        //var game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (m.map.keys.map { it.animals }.flatten().filterNotNull()
                .groupBy { it.animalType }.size == 1
        ) {
            throw UnsupportedOperationException("Cannot swap enclosures with the same animalTypes")
        }
        if (m.map.keys.any { it.animals.isEmpty() }) {
            throw UnsupportedOperationException("Cannot swap from or to an empty enclosure")
        }
    }

    /**
     * Validates Moneyaction.Discard1token
     */
    private fun validateDiscard1token(m: MoneyAction.Discard1Token, state: ZoolorettoGame? = null) {
        val game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (m.chosenCard is Card.Animal){
        if (game.currentPlayer.zoo.barn.animals.none { it == m.chosenCard }) {
            throw UnsupportedOperationException("This card is not in the players barn")
        }
        }else{
            if (game.currentPlayer.zoo.barn.shops.none{it==m.chosenCard}){
                throw UnsupportedOperationException("No shop in the players barn")
            }
        }
    }

    /**
     * Validates Moneyaction.Expandzoo
     */
    private fun validateExpandzoo(state: ZoolorettoGame? = null) {
        val game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (game.currentPlayer.zoo.expanded == 0) throw UnsupportedOperationException("Player has already expanded")
    }

    /**
     * Validates Turn.Taketruck
     */
    private fun validateTaketruck(t: Turn.TakeTruck, state: ZoolorettoGame? = null) {
        val game = state ?: requireNotNull(rootService.gameMaster.currentGame)
        if (t.truck.cards.isEmpty()) throw UnsupportedOperationException("The Truck is empty")
        if (game.players.map { it.selectedTruck }
                .contains(t.truck)) throw UnsupportedOperationException("This Truck is already selected by another player")
        if (t.map.any { it.first is Card.Animal && it.second == null }) {
            throw UnsupportedOperationException("There was an AnimalCard for which no enclosure was selected")
        }
    }

    /**
     * Prepares the next turn and checks if player is human or AI
     */
    private fun prepareNextTurn() {
        val game = requireNotNull(rootService.gameMaster.currentGame)
        if (game.players.size == game.players.mapNotNull { it.selectedTruck }.size) {
            gameHasEnded = (game.deck.size == 0 && game.players.all{it.selectedTruck != null})
            if(gameHasEnded) onAllRefreshables {
                refreshAfterGameEnd()

            }
            game.players.forEach { it.selectedTruck = null }
            game.trucks.forEach {
                it.aiConsider=true
                if (it.cards.filterNotNull().isNotEmpty()) {
                    it.cards = listOf(null, null, null).toMutableList()
                }
            }
            onAllRefreshables { refreshAfterRoundEnd() }
        }else{
            game.currentPlayer = game.players[(game.players.indexOf(game.currentPlayer) + 1) % game.players.size]
            while (game.currentPlayer.selectedTruck != null) {
                game.playedTurns.add("${game.currentPlayer} hat bereits einen Truck genommen und wird übersprungen.")
                game.currentPlayer = game.players[(game.players.indexOf(game.currentPlayer) + 1) % game.players.size]
            }
        }
        rootService.gameMaster.appendGame()
        onAllRefreshables { refreshAfterTurn() }
    }

    private fun turnString(t : Turn) : String = when(t){
        is Turn.Draw -> "eine Karte gezogen."
        is Turn.MoneyAction -> moneyActionString(t.m)
        is Turn.TakeTruck -> "einen Truck genommen."
    }

    private fun moneyActionString(m : MoneyAction) : String = when(m){
        is MoneyAction.Move1Token -> "eine Karte in seinem Zoo bewegt."
        is MoneyAction.Swap2Types -> "zwei Tierarten getauscht."
        is MoneyAction.Purchase1Token -> "eine Karte gekauft."
        is MoneyAction.Discard1Token -> "eine Karte entsogt."
        is MoneyAction.ExpandZoo -> "seinen Zoo erweitert."
        else ->"skipped !."
    }


    /**
     * Makes a turn
     *
     * @param t the type of turn that should be executed
     * @param state the state on which the turn will be executed
     */
    fun turn(t: Turn, state: ZoolorettoGame? = null) {
        val game = state ?: rootService.gameMaster.currentGame
        rootService.playerActionService.game=game
        validateTurn(t)
        rootService.playerActionService.turn(t, game)
        rootService.gameMaster.currentGame.playedTurns.add("${game.currentPlayer} hat ${turnString(t)}")
        prepareNextTurn()
    }

    /**
     * ends the Game
     */
    fun endGame()
    {
        rootService.gameService.gameHasEnded = true
        onAllRefreshables { refreshAfterGameEnd() }
    }

}