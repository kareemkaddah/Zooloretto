package service

import entity.*
import kotlin.math.max

/**
 * class that implements the logic behind decision-making for an AI in a Zooloretto game
 * @param rootService the root of running game
 */
class KIServiceV2(private var rootService: RootService) : AbstractRefreshingService() {
    private var swap2Types = Action.Swap2Types
    private var move1Token = Action.Move1Token
    private var purchase1Token = Action.Purchase1Token
    private var expandZoo = Action.ExpandZoo
    private var discard1Token = Action.Discard1Token

    /**
     * transforms an action to turn
     * @param bestTurn the action decided by the AI
     * @param game the current game
     */
    private fun fromActionToTurn(bestTurn: Action, game: ZoolorettoGame): Turn {
        when (bestTurn) {
            Action.Draw -> {
                return Turn.Draw(bestTurn.truck, bestTurn.drawIndex)
            }
            Action.TakeTruck -> {
                val pl = placeTilesMap(bestTurn.truck, game)
                return Turn.TakeTruck(bestTurn.truck, pl)
            }
            Action.Discard1Token -> {
                rootService.playerActionService
                return Turn.MoneyAction(MoneyAction.Discard1Token(bestTurn.chosenAnimal))
            }
            Action.Purchase1Token -> {
                return Turn.MoneyAction(
                    MoneyAction.Purchase1Token(
                        bestTurn.moveLocation,
                        bestTurn.sPlayer,
                        bestTurn.chosenAnimal,
                        bestTurn.index,
                    )
                )
            }
            Action.ExpandZoo -> {
                return Turn.MoneyAction(MoneyAction.ExpandZoo)
            }
            Action.Swap2Types -> {
                return Turn.MoneyAction(MoneyAction.Swap2Types(bestTurn.map))
            }
            Action.Move1Token -> {
                return Turn.MoneyAction(
                    MoneyAction.Move1Token(
                        bestTurn.moveLocation,
                        bestTurn.choseCard, bestTurn.index, bestTurn.takeFromLocation
                    )
                )
            }
            else -> return Turn.MoneyAction(MoneyAction.Skip)
        }
    }

    /**
     * returns the best calculated turn for an AI
     * @param game the current game
     * @param diff the AI-Difficulty
     */
    fun getBestTurn(game: ZoolorettoGame,diff :AIDifficulty=AIDifficulty.HARD):Turn {
        val phaseOne =phaseOne(game)
        var phaseTwo =phaseTwo(game,game.currentPlayer)
        if (diff == AIDifficulty.EASY
            && (phaseTwo.first != Action.ExpandZoo )) phaseTwo = Pair(phaseTwo.first,0.0)
        if (diff == AIDifficulty.MEDIUM
            && !(phaseTwo.first == Action.ExpandZoo
                    || phaseTwo.first == Action.Move1Token )) phaseTwo = Pair(phaseTwo.first,0.0)
        if (phaseOne.second >= phaseTwo.second) println(phaseOne.first)
        else println(phaseTwo.first)
        println(game.deck.size)
        game.players.forEach { println(it.zoo.barn.animals.filterNotNull()) }
        return if (phaseOne.second >= phaseTwo.second) fromActionToTurn(phaseOne.first, game)
        else fromActionToTurn(phaseTwo.first, game)
    }

    /**
     * an enum of all action that an AI can take in each turn and each phase along with the attributes
     * needed to execute them
     */
    enum class Action {

        Swap2Types,
        Move1Token,
        Purchase1Token,
        Discard1Token,
        ExpandZoo,
        Draw,
        TakeTruck,
        Skip;

        lateinit var map: Map<Enclosure, AnimalType>
        lateinit var moveLocation: Enclosure
        lateinit var choseCard: Card
        lateinit var sPlayer: Player
        lateinit var chosenAnimal: Card.Animal
        lateinit var truck: Truck
        var takeFromLocation: Enclosure? = null
        var index: Int = 0
        var drawIndex = 0

    }

    /**
     * calculates the scoring after each money action and
     * decides which one an AI should take this turn and executes it
     * @param game the current Game
     * @param player corresponds to the player in action
     */
    fun phaseTwo(game: ZoolorettoGame, player: Player): Pair<Action, Double> {
        val actionMap: MutableMap<Action, Double> = mutableMapOf(
            move1Token to move1TokenScore(player),
            expandZoo to expandZooScore(player),
            discard1Token to discard1TokenScore(game, player),
            purchase1Token to purchase1TokenScore(game, player),
            swap2Types to swap2Types(player)
        )
        val bestAction = actionMap.maxByOrNull { it.value }
        if (checkNotNull(bestAction).value != 0.0) {
            return bestAction.toPair()
        }
        return Pair(Action.Skip, 0.0)
    }

    /**
     * returns the score for swap 2 types
     * criteria : swapping from
     * barn -> enclosure or full enclosure -> not full & not empty enclosure
     * @param player player in action
     */
    private fun swap2Types(player: Player): Double {
        if (player.balance < 1) return 0.0
        var score = 0.0
        if (player.zoo.expanded == 0) {
            for (animal in player.zoo.barn.animals.filterNotNull()) {
                val animalType = mutableListOf<Card.Animal>()
                for (tile in player.zoo.barn.animals.filterNotNull()) {
                    if (tile.animalType == animal.animalType) {
                        animalType.add(tile)
                    }
                }
                for (enclosure in player.zoo.enclosures.filterNot {
                    it.isBarn || it.animals.filterNotNull().size ==animalType.size
                }) {
                    if (enclosure.animals.filterNotNull().isNotEmpty()) {
                        val enclosureType = enclosure.animals.filterNotNull().first().animalType
                        val enclosureSize = enclosure.animals.filterNotNull().size
                        val animalNum = animalType.size
                        if (animalNum > enclosureSize) {
                            if (animalNum <= enclosure.animalCapacity) {
                                val bonus = (animalNum - enclosureSize) * 1.0 +
                                        if (
                                            enclosure.shops.filterNotNull().isNotEmpty() &&
                                            animalNum < enclosure.animalCapacity - 1
                                        ) 0.5
                                        else 0.0
                                if (bonus > score) {
                                    score = bonus
                                    swap2Types.map = mapOf(
                                        player.zoo.barn to animal.animalType,
                                        enclosure to enclosureType
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        val fullSpace = player.zoo.enclosures.filter { !it.isBarn &&
        it.animals.filterNotNull().size==it.animalCapacity}
        val notFullSpaces=player.zoo.enclosures.filter { !it.isBarn &&
                it.animals.filterNotNull().isNotEmpty() &&
                it.animals.filterNotNull().size < it.animalCapacity - 1 }
        for(full in fullSpace){
            for(notFull in notFullSpaces){
                if(full.animalCapacity >= notFull.animals.filterNotNull().size &&
                    full.animals.filterNotNull().size <= notFull.animalCapacity
                    && full.animalCapacity < notFull.animalCapacity
                    && ( full.animals.filterNotNull().first().animalType
                    != notFull.animals.filterNotNull().first().animalType )
                    && full.animalCapacity != notFull.animals.filterNotNull().size
                ) {
                    val bonus = when(notFull.animals.filterNotNull().size) {
                        full.animalCapacity -> full.pointsFull
                        full.animalCapacity - 1 -> full.pointsNearlyFull
                        else -> notFull.animals.filterNotNull().size
                    } * if(
                        full.shops.filterNotNull().isNotEmpty() &&
                        notFull.animals.filterNotNull().size < full.animalCapacity-1
                    ) 1.0
                    else 0.5
                    if (bonus > score) {
                        score = bonus
                        val notFullAnimal = requireNotNull(notFull.animals.filterNotNull().first())
                        val fullAnimal = requireNotNull(full.animals.filterNotNull().first())
                        swap2Types.map = mapOf(
                            notFull to notFullAnimal.animalType,
                            full to fullAnimal.animalType
                        )
                    }
                }
            }
        }
        return score
    }

    /**
     * returns the score of purchase 1 token
     * criteria : purchasing a token from :
     * other player's barn -> [originalPlayer] 's enclosure ; the one with the biggest number this token
     * @param game the current game
     * @param originalPlayer the player in action
     */
    private fun purchase1TokenScore(game: ZoolorettoGame, originalPlayer: Player): Double {
        if (originalPlayer.balance < 2) return 0.0
        var score = 0.0
        for (player in game.players) {
            if (player != originalPlayer) {
                for (token in player.zoo.barn.animals.filterNotNull()) {
                    val enclosuresHasAnimal = originalPlayer.zoo.enclosures.filter {
                        !it.isBarn &&
                                it.animals.filterNotNull().isNotEmpty()
                                && it.animals.filterNotNull().first().animalType == token.animalType &&
                                (it.animalCapacity > it.animals.filterNotNull().size)
                    }
                    for (e in enclosuresHasAnimal) {
                        val nextIndex = e.animals.indexOf(null)
                        val size = e.animals.filterNotNull().size
                        if (size + 1 == e.animalCapacity) {
                            if (e.pointsFull > score) {
                                score = e.pointsFull * 1.0
                                purchase1Token.moveLocation = e
                                purchase1Token.sPlayer = player
                                purchase1Token.chosenAnimal = token
                                purchase1Token.index = nextIndex
                            }
                        } else if (size + 1 == e.animalCapacity - 1) {
                            if (e.pointsNearlyFull > score) {
                                score = e.pointsNearlyFull * 1.0
                                purchase1Token.moveLocation = e
                                purchase1Token.sPlayer = player
                                purchase1Token.chosenAnimal = token
                                purchase1Token.index = nextIndex
                            }
                        } else {
                            if (size + 1.0 > score && e.shops.filterNotNull().isNotEmpty()) {
                                score = size + 2.0
                                purchase1Token.moveLocation = e
                                purchase1Token.sPlayer = player
                                purchase1Token.chosenAnimal = token
                                purchase1Token.index = nextIndex
                            } else {
                                if (size * 0.5 + 1.0 > score) {
                                    score = size * 0.5 + 1.0
                                    purchase1Token.moveLocation = e
                                    purchase1Token.sPlayer = player
                                    purchase1Token.chosenAnimal = token
                                    purchase1Token.index = nextIndex
                                }
                            }
                        }
                    }
                }
            }
        }
        return score
    }

    /**
     * returns the score for discard 1 token
     * criteria : will discard an animal from [player] 's barn if:
     * 1 : there is only one kind of this animal present in barn
     * 2 : there is not an empty space for this animal
     * 3: [player] cannot expand his zoo any further
     * @param game the current game
     * @param player player in action
     */
    private fun discard1TokenScore(game: ZoolorettoGame, player: Player): Double {
        if (player.balance < 2) return 0.0
        var bonus = 0.0
        if (!hasEmptySpaceForNewAnimal(player.zoo.enclosures)
            && player.zoo.expanded == 0
        ) {
            var animalType: Card.Animal?
            for (animal in player.zoo.barn.animals.filterNotNull()) {
                var count = 0
                animalType = animal
                for (tile in player.zoo.barn.animals.filterNotNull()) {
                    if (animal.animalType == tile.animalType) count++
                }
                if (count==1 ) {
                    if( (!isTradableAnimal(animal,game,player) && player.zoo.expanded != 0)
                        || game.deck.size < 5
                    ) {
                        if( 5.0 > bonus ) {
                            bonus = 5.0
                            discard1Token.chosenAnimal= animalType
                        }
                    }
                }
            }
        }
        return bonus
    }

    /**
     * returns 4.0 as a score if the player should expand his zoo (and can)
     * criteria :
     * 1 : if there is no empty space for animal
     * 2 : if this animal is distinct animal type
     * 3 : zoo is expandable
     * 4 : all enclosure are full
     * @param player player in action
     */
    private fun expandZooScore(player: Player): Double {
        if (player.balance < 3 || player.zoo.expanded == 0) return 0.0
        val animals = player.zoo.barn.animals.filterNotNull()
        if (!hasEmptySpaceForNewAnimal(player.zoo.enclosures) && animals.isNotEmpty()) {
            var distinctAnimal = true
            for (animal in animals) {
                player.zoo.enclosures.forEach {
                    if (!it.isBarn
                        && it.animals.filterNotNull().first().animalType == animal.animalType
                    ) {
                        distinctAnimal = false
                    }
                }
                if (distinctAnimal) return 4.0
            }
        }
        if (!hasEmptySpaceForNewAnimal(player.zoo.enclosures)) return 4.0
        return 0.0
    }

    /**
     * returns the score for move 1 token
     * criteria : barn -> enclosure
     * for : animals
     * prioritize enclosure that contains this animal type & is not full
     * otherwise move to an empty one
     * for shops :
     * prioritize enclosures with most animals and doesn't have a shop
     * otherwise to an empty one
     * enclosure -> enclosure : only for shops:
     * moves a shop from an enclosure where this shop is no longer needed to another
     * enclosure where it is needed to improve the score
     * @param player player in action
     */
    private fun move1TokenScore(player: Player): Double {
        if (player.balance < 1) return 0.0
        var bonus = 0.0
        for (token in player.zoo.barn.animals.filterNotNull()) {
            val enclosuresHasAnimal = player.zoo.enclosures.filter {
                it.animals.filterNotNull().isNotEmpty() &&
                        !it.isBarn && it.animals.filterNotNull().first().animalType == token.animalType
                        && (it.animalCapacity > it.animals.filterNotNull().size)
            }
            if (enclosuresHasAnimal.isNotEmpty()) {
                for (e in enclosuresHasAnimal) {
                    val nextIndex = e.animals.filterNotNull().lastIndex + 1
                    val size = e.animals.filterNotNull().size
                    if (size + 1 == e.animalCapacity && e.pointsFull > bonus) {
                        bonus = e.pointsFull * 1.0
                        move1Token.moveLocation = e
                        move1Token.choseCard = token
                        move1Token.index = nextIndex
                        move1Token.takeFromLocation = null
                    } else if (size + 1 == e.animalCapacity - 1 && e.pointsNearlyFull > bonus) {
                        bonus = e.pointsNearlyFull * 1.0
                        move1Token.moveLocation = e
                        move1Token.choseCard = token
                        move1Token.index = nextIndex
                        move1Token.takeFromLocation = null
                    } else {
                        if (size + 1 > bonus) {
                            bonus = size + 1.0
                            move1Token.moveLocation = e
                            move1Token.choseCard = token
                            move1Token.index = nextIndex
                            move1Token.takeFromLocation = null
                        }
                    }
                }
            } else {
                if (hasEmptySpaceForNewAnimal(player.zoo.enclosures) && 3.0 > bonus) {
                    bonus = 3.0
                    move1Token.moveLocation = player.zoo.enclosures
                        .first { !it.isBarn && it.animals.filterNotNull().isEmpty() }
                    move1Token.choseCard = token
                    move1Token.index = move1Token.moveLocation.animals.filterNotNull().lastIndex + 1
                    move1Token.takeFromLocation = null
                }
            }
        }
        for (token in player.zoo.barn.shops.filterNotNull()) {
            val enclosures = player.zoo.enclosures.filter {
                !it.isBarn && it.shops.filterNotNull().size < it.shopCapacity
                        && it.animals.filterNotNull().size < it.animalCapacity - 1
            }
            for (e in enclosures) {
                val size = e.shops.filterNotNull().size
                if (size == 0 && e.animals.filterNotNull().isNotEmpty()
                    && 1 + e.animals.filterNotNull().size > bonus
                ) {
                    bonus = 1 + e.animals.filterNotNull().size * 0.5
                    move1Token.moveLocation = e
                    move1Token.choseCard = token
                    move1Token.index = 0
                    move1Token.takeFromLocation = null
                } else {
                    if (1 > bonus) {
                        bonus = 1.0
                        move1Token.moveLocation = e
                        move1Token.choseCard = token
                        move1Token.index = e.animals.filterNotNull().lastIndex + 1
                        move1Token.takeFromLocation = null
                    }
                }
            }
        }
        val shopToTakeFrom = player.zoo.enclosures.filter {
            !it.isBarn &&
                    it.animals.filterNotNull().size >= it.animalCapacity - 1 && it.shops.filterNotNull().isNotEmpty()
        }
        val shopToMoveTo = player.zoo.enclosures.filter {
            !it.isBarn &&
                    it.animals.filterNotNull().isNotEmpty() &&
                    it.animals.filterNotNull().size < it.animalCapacity - 1 && it.shops.filterNotNull().isEmpty()
        }
        for (take in shopToTakeFrom) {
            for (move in shopToMoveTo) {
                if (move.animals.filterNotNull().size > bonus) {
                    bonus = move.animals.filterNotNull().size * 1.0
                    move1Token.moveLocation = move
                    move1Token.choseCard = take.shops.filterNotNull().first()
                    move1Token.index = 0
                    move1Token.takeFromLocation = take
                }
            }
        }
        return bonus
    }

    /**
     * executes the best action : draw or take truck which has the best predicted outcome
     * this is decided with how deep the AI will simulate game
     * and the cards that the AI will be considering
     * note that :the more the deck has fewer cards the more the prediction are better
     */
    private fun phaseOne(currentGame: ZoolorettoGame): Pair<Action, Double> {
        val currentPlayer = currentGame.currentPlayer
        val depth = 8 -currentGame.players.size
        var bestRanking = Double.NEGATIVE_INFINITY
        var bestMove: Action? = null
        val draw = Action.Draw
        var drawScore = 0
        currentGame.trucks.filterNot { !it.aiConsider }
            .forEach { drawScore += (it.capacity - it.cards.filterNotNull().size) * 2 }
        if (drawScore != 0) {
            for (tile in getMostLikelyToDraw(currentGame)) {
                val truck = bestTruckToPlaceON(currentGame, currentPlayer, tile)
                val gameAfterDraw = shallowCopy(currentGame)
                val deck = gameAfterDraw.deck + gameAfterDraw.extraCards
                drawNPlace(
                    gameAfterDraw,
                    deck[(currentGame.deck + currentGame.extraCards).indexOf(tile)],
                    gameAfterDraw.trucks[currentGame.trucks.indexOf(truck)]
                )
                val drawRanking = miniMax(
                    gameAfterDraw, depth - 1, currentGame.players.indexOf(currentPlayer),
                    drawScore - 2
                ) - score(currentPlayer)
                if (drawRanking >= bestRanking) {
                    bestRanking = drawRanking
                    draw.truck = truck
                    draw.drawIndex = truck.cards.filterNotNull().lastIndex + 1
                    bestMove = draw
                }
            }
        }
        val bestTruck = bestTruckNScore(currentGame, currentPlayer)
        if (bestTruck.second!!.cards.filterNotNull().isNotEmpty()) {
            val gameAfterTakeTruck = shallowCopy(currentGame)
            takeTruck(gameAfterTakeTruck.trucks[currentGame.trucks.indexOf(bestTruck.second)]
                ,gameAfterTakeTruck)
            val takeTruckRanking = miniMax(gameAfterTakeTruck,0,currentGame.players.indexOf(currentPlayer),
                (drawScore- (bestTruck.second!!.capacity-bestTruck.second!!.cards.filterNotNull().size)*2)) - score(currentPlayer)
            if((takeTruckRanking > bestRanking && bestTruck.first >drawScore+2) || bestMove == null){
                bestRanking=takeTruckRanking
                val takeTruck =Action.TakeTruck
                takeTruck.truck=bestTruck.second!!
                bestMove =takeTruck
            }
        }
        return if (bestRanking < 0) Pair(bestMove!!, 0.0)
        else Pair(bestMove!!, bestRanking)
    }

    /**
     * returns a mapping for each card in the truck [second] : which enclosure it should go to & at which index
     * @param second truck that is considered
     * @param game the current game
     */
    fun placeTilesMap(second: Truck, game: ZoolorettoGame): List<Triple<Card, Enclosure, Int>> {
        val copyGame = shallowCopy(game)
        val enclosuresMap =
            copyGame.currentPlayer.zoo.enclosures.associateWith {
                game.currentPlayer.zoo.enclosures[copyGame.currentPlayer.zoo.enclosures.indexOf(it)]
            }
        val map: MutableList<Triple<Card, Enclosure, Int>> = mutableListOf()
        second.cards.filterNotNull().forEach { card ->
            val pair = assignAnEnclosure(card, enclosuresMap, game.currentPlayer.zoo)
            map.add(Triple(card, pair.first, pair.second))
        }
        return map
    }

    /**
     * return for a given [card] the enclosure it should go to along with the index
     * @param card card that is considered
     * @param map mapping of enclosures from a copy game : key associated with same one in the original one
     */
    private fun assignAnEnclosure(card: Card, map: Map<Enclosure, Enclosure>, zoo: Zoo): Pair<Enclosure, Int> {
        when (card) {
            is Card.Animal -> {
                val e =map.toList().sortedBy { it.first.animalCapacity }
                for (enclosure in e.filter { !it.first.isBarn && (it.first.animals.filterNotNull().isEmpty() ||
                        (it.first.animals.filterNotNull().size < it.first.animalCapacity &&
                                it.first.animals.filterNotNull().first().animalType==card.animalType ))}){
                    val index = enclosure.first.animals.filterNotNull().lastIndex+1
                    enclosure.first.animals.add(index,card)
                    return Pair(map[enclosure.first]!!,index)
                }
                val barn = requireNotNull(map.keys.find { it.isBarn })
                val index = barn.animals.filterNotNull().lastIndex + 1
                barn.animals[index] = card
                return Pair(zoo.barn, index)
            }
            is Card.Shop-> {
                val e = map.toList().sortedByDescending { it.first.animalCapacity }
                for (enclosure in e.filter { !it.first.isBarn && it.first.shops.filterNotNull().size < it.first.shopCapacity
                        && it.first.animals.filterNotNull().size < it.first.animalCapacity - 1
                }){
                    val index = enclosure.first.shops.filterNotNull().lastIndex+1
                    enclosure.first.shops.add(index,card)
                    return Pair(enclosure.second,index)
                }
                val barn = requireNotNull(map.keys.find { it.isBarn })
                val index = barn.shops.filterNotNull().lastIndex + 1
                barn.shops[index] = card
                return Pair(zoo.barn, index)
            }
            else -> return Pair(map.keys.first(), 0)
        }
    }

    /**
     * function responsible for simulating the game and returning
     * the best outcome for current player : player at index [maximizingPlayerIndex]
     * @param game the current game
     * @param depth how far the simulation should go : each player action is done in 1 unit in depth
     * @param maximizingPlayerIndex the index of the player in the actual current game as well as the maximizing one
     * @param drawScore variable that prevents a player from drawing when all trucks are full and
     * improves take truck decisions : somewhat of a shortcut or a way to allow for more depth in theory (lmao)
     */
    private fun miniMax(game: ZoolorettoGame?, depth: Int, maximizingPlayerIndex: Int, drawScore: Int): Double {
        checkNotNull(game)
        var drawScoreRound = drawScore
        if (depth == 0 || game.deck.isEmpty() || allHasTruck(game)) {
            if (allHasTruck(game)) {
                game.players.forEach { it.selectedTruck = null }
                game.trucks.forEach { it.aiConsider = true }
            }
            val maximizingPlayer = game.players[maximizingPlayerIndex]
            return score(maximizingPlayer)
        }
        game.currentPlayer = nextPlayer(game.currentPlayer, game)
        if (game.currentPlayer.selectedTruck != null && game.currentPlayer.selectedTruck!!.cards.filterNotNull()
                .isEmpty()
        ) {
            return miniMax(game, depth - 1, maximizingPlayerIndex, drawScoreRound)
        }

        if (game.currentPlayer == game.players[maximizingPlayerIndex]) {
            var bestScore = Double.NEGATIVE_INFINITY
            if (drawScoreRound > 0) {
                for (tile in getMostLikelyToDraw(game)) {
                    val truck = bestTruckToPlaceON(game, game.currentPlayer, tile)
                    val gameAfterDraw = shallowCopy(game)
                    drawNPlace(
                        gameAfterDraw,
                        gameAfterDraw.deck[game.deck.indexOf(tile)],
                        gameAfterDraw.trucks[game.trucks.indexOf(truck)]
                    )
                    val score = miniMax(
                        gameAfterDraw, depth - 1, maximizingPlayerIndex,
                        drawScoreRound - 2
                    )
                    bestScore = max(score, bestScore)
                }
            }
            val bestTruck = bestTruckNScore(game, game.currentPlayer)
            val gameAfterTakeTruck = shallowCopy(game)
            takeTruck(
                gameAfterTakeTruck.trucks[game.trucks.indexOf(bestTruck.second)], gameAfterTakeTruck
            )
            val score = miniMax(
                gameAfterTakeTruck, 0, maximizingPlayerIndex,
                (drawScoreRound - (bestTruck.second!!.capacity - bestTruck.second!!.cards.filterNotNull().size) * 2)
            )
            bestScore = max(bestScore, score)
            return bestScore
        } else {
            var bestScore = Double.POSITIVE_INFINITY
            if (drawScoreRound > 0) {
                for (tile in getMostLikelyToDraw(game)) {
                    val truck = bestTruckToPlaceON(game, game.currentPlayer, tile)
                    if (truck.cards.filterNotNull().size < truck.capacity) {
                        val gameAfterDraw = shallowCopy(game)
                        drawNPlace(
                            gameAfterDraw,
                            gameAfterDraw.deck[game.deck.indexOf(tile)],
                            gameAfterDraw.trucks[game.trucks.indexOf(truck)]
                        )
                        val score = miniMax(
                            gameAfterDraw, depth - 1, maximizingPlayerIndex,
                            drawScoreRound - 2
                        )
                        bestScore = kotlin.math.min(score, bestScore)
                    }
                }
            }
            val bestTruck = bestTruckNScore(game, game.currentPlayer)
            val gameAfterTakeTruck = shallowCopy(game)
            takeTruck(
                gameAfterTakeTruck.trucks[game.trucks.indexOf(bestTruck.second)], gameAfterTakeTruck
            )
            val score = miniMax(
                gameAfterTakeTruck, 0, maximizingPlayerIndex,
                (drawScoreRound - (bestTruck.second!!.capacity - bestTruck.second!!.cards.filterNotNull().size) * 2)
            )
            bestScore = kotlin.math.min(bestScore, score)
            return bestScore
        }
    }

    /**
     * return the score after simple calculation ( criteria for score's reduction is ignored here)
     * @param player player in action
     */
    private fun score(player: Player): Double {
        var score = 0.0
        player.zoo.enclosures.filterNot { it.isBarn }.forEach {
            score += when (it.animals.filterNotNull().size) {
                it.animalCapacity -> it.pointsFull * 1.0
                it.animalCapacity - 1 -> it.pointsNearlyFull * 1.0
                else -> it.animals.filterNotNull().size * when (it.shops.filterNotNull().size) {
                    0 -> 0.5
                    else -> 1.0
                }
            }
        }
        score += player.zoo.enclosures.filterNot { it.isBarn }.flatMap { it.shops.filterNotNull() }
            .distinct().size * 0.5
        score += player.balance * 0.5
        return score
    }

    /**
     * returns a number of random cards from game's [deck]
     * note that this not done with statistics since there
     * is basically not too much of difference if the drawn amount is note small!
     * the quality of cards is improved the further the game go
     * @param game the current game
     */
    private fun getMostLikelyToDraw(game: ZoolorettoGame): MutableList<Card> {
        val randomDeck = mutableListOf<Card>()
        repeat(6) {
            if (game.deck.isNotEmpty()) randomDeck.add(game.deck.random())
            else randomDeck.add(game.extraCards.random())
        }
        return randomDeck
    }

    /**
     * takes the truck [second] from [gameState] and adds it to the current player
     * and then unload the cards into the player's zoo
     * @param second the truck that will be taken
     * @param gameState the current game
     */
    fun takeTruck(second: Truck?, gameState: ZoolorettoGame) {
        val pl = placeTilesMap(second!!, gameState)
        rootService.playerActionService.turn(
            Turn.TakeTruck(
                second, pl
            ), gameState
        )
    }

    /**
     * draws a [tile] from [gameState]'s deck and loads it into [truck]
     * @param gameState current game
     * @param tile the drawn card
     * @param truck the truck that will have this tile
     */
    fun drawNPlace(gameState: ZoolorettoGame, tile: Card, truck: Truck) {
        truck.cards.add(truck.cards.lastIndex + 1, tile)
        gameState.deck.remove(tile)
    }

    /**
     * update the [game]'s current player
     * @param player the current player
     * @param game the current game
     */
    fun nextPlayer(player: Player, game: ZoolorettoGame): Player {
        val playerNumber = game.players.size
        val playerIndex = game.players.indexOf(player)
        return game.players[(playerIndex + 1) % playerNumber]
    }

    /**
     * returns true if all player in [game] have taken a truck
     * false otherwise
     * @param game the current game
     */
    fun allHasTruck(game: ZoolorettoGame): Boolean {
        for (player in game.players) {
            if (player.selectedTruck == null)
                return false
        }
        return true
    }

    /**
     * return the best truck to load  a [tile] on it for [player]
     * @param game the current game
     * @param player the considered player
     * @param tile the drawn card
     */
    private fun bestTruckToPlaceON(game: ZoolorettoGame, player: Player, tile: Card): Truck {
        val gameCopy = shallowCopy(game)
        val indexMap = gameCopy.trucks.associateWith { gameCopy.trucks.indexOf(it) }
        game.trucks.filterNot { it.cards.filterNotNull().size == it.capacity || !it.aiConsider }
        gameCopy.trucks =
            gameCopy.trucks.filterNot {
                it.cards.filterNotNull().size == it.capacity || !it.aiConsider
            } as MutableList<Truck>
        var cardCount = 0
        game.trucks.forEach { cardCount += it.cards.filterNotNull().size }
        var maxCount = 0
        game.trucks.forEach { maxCount += it.capacity }
        gameCopy.trucks.shuffle()
        var truckScores = mutableListOf<Pair<Double, Truck?>>()
        if (cardCount * 2 > maxCount) {
            gameCopy.trucks.forEach {
                it.cards.add(it.cards.filterNotNull().lastIndex + 1, tile)
            }
            repeat(gameCopy.trucks.size) {
                val truck = bestTruckNScore(gameCopy, player)
                truckScores.add(truck)
                gameCopy.trucks.remove(truck.second)
            }
            truckScores.sortByDescending { it.first }
        } else {
            repeat(gameCopy.trucks.size) {
                val truck = bestTruckNScore(gameCopy, nextPlayer(player, game))
                truckScores.add(truck)
                gameCopy.trucks.remove(truck.second)
            }
            truckScores = truckScores.sortedBy { it.first }.toMutableList()
        }
        //val index=indexMap[truckScores.first().second]!!
        return game.trucks[indexMap[truckScores.first().second]!!]
    }

    /**
     * return the best truck available in [game] as well as it's score for [player]
     * criteria :
     *I: if the card is an animal :
     * 1 : how many animals in enclosure with same type (enclosure with the biggest size is prioritized)
     * 2 : if that enclosure has shops
     * 3 : otherwise considers empty enclosures
     * 4 : is tradable animal
     *II: if the card is shop :
     * 1 : considers biggest enclosure without shops
     * 2 : otherwise empty ones
     *III: is coins : + points
     * if none above : the scores receives - points (usually when the card is thrown into the barn)
     */
    fun bestTruckNScore(game: ZoolorettoGame, player: Player): Pair<Double, Truck?> {
        var bestTruck: Pair<Double, Truck?> = Pair(-10.0, game.trucks.maxByOrNull { it.cards.filterNotNull().size }!!)
        val gameCopy = shallowCopy(game)
        val playerCopy = gameCopy.players[game.players.indexOf(player)]
        for (truck in gameCopy.trucks.filterNot { !it.aiConsider }) {
            var bonus = 0.0
            for (tile in truck.cards.filterNotNull()) {
                when (tile) {
                    is Card.Animal -> {
                        val enclosures = thatHasThisAnimal(tile, playerCopy.zoo.enclosures)
                        enclosures.removeAll { it.first.animalCapacity == it.first.animals.filterNotNull().size }
                        if (enclosures.isNotEmpty()) {
                            enclosures.sortByDescending { it.second }
                            val enclosure = enclosures.first().first
                            enclosure.animals[enclosure.animals.filterNotNull().lastIndex + 1] = tile
                            bonus += if (enclosure.animals.filterNotNull().size == enclosure.animalCapacity - 1) {
                                enclosure.pointsNearlyFull * 1.0
                            } else if (enclosure.animals.filterNotNull().size == enclosure.animalCapacity) {
                                enclosure.pointsFull * 1.0
                            } else {
                                if (enclosure.shops.filterNotNull().isNotEmpty()) {
                                    (enclosure.pointsNearlyFull * 1.0)
                                } else {
                                    (enclosure.pointsNearlyFull - 1.0)
                                }
                            }

                        } else {
                            if (hasEmptySpaceForNewAnimal(playerCopy.zoo.enclosures)) {
                                bonus += 2.0
                                val enclosure = playerCopy.zoo.enclosures.first { it.animals.filterNotNull().isEmpty() }
                                enclosure.animals.add(0, tile)
                            } else if (isTradableAnimal(tile, gameCopy, playerCopy)) {
                                bonus += 1.0
                            } else {
                                bonus += -2.0
                            }
                        }
                    }
                    is Card.Shop -> {
                        val multiplier = player.zoo.enclosures.filterNot { it.isBarn && it.shops.filterNotNull().isNotEmpty() &&
                                it.animals.filterNotNull().size<it.animalCapacity-1 }.sortedByDescending {
                            it.animals.filterNotNull().size }.first().animals.filterNotNull().size-1
                        bonus += if(hasEmptySpaceForShop(playerCopy.zoo.enclosures)){
                            if (!hasThisShop(tile,playerCopy.zoo.enclosures)){
                                1.0*multiplier
                            } else 0.5*multiplier
                        }else -2.0
                    }
                    else -> {val gameCopy2=shallowCopy(game)
                        gameCopy2.currentPlayer.balance=3
                        if(phaseTwo(gameCopy,gameCopy2.currentPlayer).second!=0.0 &&phaseTwo(game,game.currentPlayer).second==0.0){
                            bonus+=2.5
                        }else{
                            bonus+=2
                        }
                        //if(player.zoo.expanded!=0 && !hasEmptySpaceForNewAnimal(player.zoo.enclosures)) bonus+=3
                        //else bonus += 2.0
                    }
                }
            }
            bonus -= (3.0 - truck.cards.filterNotNull().size)
            if (bonus >= bestTruck.first) bestTruck = Pair(bonus, game.trucks[gameCopy.trucks.indexOf(truck)])
        }
        return bestTruck
    }

    /**
     * returns true if given shop [tile] is unique in [enclosures]
     * false otherwise
     * @param tile the considered shop
     * @param enclosures where will be compared
     */
    fun hasThisShop(tile: Card.Shop, enclosures: MutableList<Enclosure>): Boolean {
        for (enclosure in enclosures) {
            if (!enclosure.isBarn) {
                for (shop in enclosure.shops.filterNotNull()) {
                    if (shop.shopType == tile.shopType) return true
                }
            }
        }
        return false
    }

    /**
     * returns true if there is an empty slot for a shop
     * false otherwise
     * @param enclosures where will be searched
     */
    fun hasEmptySpaceForShop(enclosures: MutableList<Enclosure>): Boolean {
        for (enclosure in enclosures) {
            if (!enclosure.isBarn && enclosure.shops.filterNotNull().isEmpty()) return true
        }
        return false
    }

    /**
     * return true if animal [tile] is tradable (if other players have this animal in an enclosure)
     * false otherwise
     * @param tile animal that will be looked
     * @param game the current player
     */
    fun isTradableAnimal(tile: Card.Animal, game: ZoolorettoGame, p: Player): Boolean {
        for (player in game.players) {
            if (player != p) {
                for (enclosure in player.zoo.enclosures) {
                    if (!enclosure.isBarn && enclosure.animals.filterNotNull().isNotEmpty()
                        && enclosure.animals.filterNotNull().first().animalType == tile.animalType
                        && enclosure.animals.filterNotNull().size + 1 <= enclosure.animalCapacity
                    )
                        return true
                }
            }
        }
        return false
    }

    /**
     * returns true if there is an empty space
     * fasle otherwise
     * @param enclosures where an empty space will be looked for
     */
    fun hasEmptySpaceForNewAnimal(enclosures: MutableList<Enclosure>): Boolean {
        for (enclosure in enclosures) {
            if (!enclosure.isBarn && enclosure.animals.filterNotNull().isEmpty()) return true
        }
        return false
    }

    /**
     * retruns all enclosures in [enclosures] that have this animal [tile] along with the number of this animal
     * @param tile animal that is considered
     * @param enclosures where the animal is looked for
     */
    fun thatHasThisAnimal(tile: Card.Animal, enclosures: MutableList<Enclosure>): MutableList<Pair<Enclosure, Int>> {
        val list = mutableListOf<Pair<Enclosure, Int>>()
        for (enclosure in enclosures) {
            if (!enclosure.isBarn) {
                val count = enclosure.animals.filterNotNull().count { //it !=null &&
                    it.animalType == tile.animalType
                }
                if (count != 0) list.add(Pair(enclosure, count))
            }
        }
        return list
    }

    /**
     * returns a shallow copy of the given [game]
     * @param game the game that will be copied
     */
    private fun shallowCopy(game: ZoolorettoGame): ZoolorettoGame {
        val players: MutableList<Player> = mutableListOf()
        val copyGame =
            ZoolorettoGame(mutableListOf(Player.HumanPlayer("N1")), Player.HumanPlayer("N1"), game.simulationSpeed)
        game.players.forEach { player ->
            if (player is Player.AIPlayer) {
                val p = Player.AIPlayer(player.name, player.difficulty, player.speed)
                p.balance = player.balance
                p.selectedTruck = player.selectedTruck
                val zoo = Zoo(game.players.size)
                while (zoo.enclosures.size < player.zoo.enclosures.size) {
                    zoo.expand()
                }
                zoo.enclosures.forEach {
                    it.animals.removeAll { true }
                    it.shops.removeAll { true }
                }
                for (i in 0..zoo.enclosures.lastIndex) {
                    zoo.enclosures[i].isBarn = player.zoo.enclosures[i].isBarn
                    player.zoo.enclosures[i].animals.forEach {
                        if (it == null) zoo.enclosures[i].animals.add(null)
                        else zoo.enclosures[i].animals.add(Card.Animal(it.animalType, it.maturity, it.isFertile))
                    }
                    player.zoo.enclosures[i].shops.forEach {
                        if (it == null) zoo.enclosures[i].shops.add(null)
                        else zoo.enclosures[i].shops.add(Card.Shop(it.shopType))
                    }
                    zoo.enclosures[i].coinsFull = player.zoo.enclosures[i].coinsFull
                }
                zoo.barn = zoo.enclosures[0]
                p.zoo = zoo
                players.add(p)
            } else if (player is Player.HumanPlayer) {
                val p = Player.HumanPlayer(player.name)
                p.balance = player.balance
                p.selectedTruck = player.selectedTruck
                val zoo = Zoo(game.players.size)
                while (zoo.enclosures.size < player.zoo.enclosures.size) {
                    zoo.expand()
                }
                zoo.enclosures.forEach {
                    it.animals.removeAll { true }
                    it.shops.removeAll { true }
                }
                for (i in 0..zoo.enclosures.lastIndex) {
                    zoo.enclosures[i].isBarn = player.zoo.enclosures[i].isBarn
                    player.zoo.enclosures[i].animals.forEach {
                        if (it == null) zoo.enclosures[i].animals.add(null)
                        else zoo.enclosures[i].animals.add(Card.Animal(it.animalType, it.maturity, it.isFertile))
                    }
                    player.zoo.enclosures[i].shops.forEach {
                        if (it == null) zoo.enclosures[i].shops.add(null)
                        else zoo.enclosures[i].shops.add(Card.Shop(it.shopType))
                    }
                    zoo.enclosures[i].coinsFull = player.zoo.enclosures[i].coinsFull
                }
                zoo.barn = zoo.enclosures[0]
                p.zoo = zoo
                players.add(p)
            }
        }
        copyGame.players = players
        copyGame.currentPlayer = players[game.players.indexOf(game.currentPlayer)]
        game.deck.forEach {
            when (it) {
                is Card.Animal -> copyGame.deck.add(Card.Animal(it.animalType, it.maturity, it.isFertile))
                is Card.Shop -> copyGame.deck.add(Card.Shop(it.shopType))
                else -> copyGame.deck.add(Card.Coin)
            }
        }
        copyGame.extraCards = mutableListOf()
        game.extraCards.forEach {
            when (it) {
                is Card.Animal -> copyGame.extraCards.add(Card.Animal(it.animalType, it.maturity, it.isFertile))
                is Card.Shop -> copyGame.extraCards.add(Card.Shop(it.shopType))
                else -> copyGame.extraCards.add(Card.Coin)
            }
        }
        copyGame.bankCoins = game.bankCoins
        copyGame.trucks = mutableListOf()

        game.trucks.forEach { truck ->
            val cards: MutableList<Card?> = mutableListOf()
            truck.cards.filterNotNull().forEach {
                cards.add(
                    when (it) {
                        is Card.Animal -> Card.Animal(it.animalType, it.maturity, it.isFertile)
                        is Card.Shop -> Card.Shop(it.shopType)
                        else -> Card.Coin
                    }
                )
            }
            copyGame.trucks.add(Truck(cards, truck.capacity, truck.aiConsider))
        }

        return copyGame
    }

}
