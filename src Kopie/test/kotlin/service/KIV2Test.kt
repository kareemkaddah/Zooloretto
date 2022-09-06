package service

import entity.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * class where the test for KIServiceV2 take place
 */

class KIV2Test {

    /**
     * here the game will be progressed slowly and tests for different functions are done separately
     */
    @Test
    fun progressiveTest (){
        val rootService = RootService()
        val currentGame =rootService.gameMaster.currentGame
         val ki =rootService.kiServiceV2
        rootService.playerActionService.game=currentGame
        initializeGameMaster(currentGame, 3)
        // test for has this animal
        assertFalse(ki.thatHasThisAnimal(Card.Animal(AnimalType.Panda,Maturity.Male)
            ,currentGame.currentPlayer.zoo.enclosures).isNotEmpty())
        currentGame.currentPlayer.zoo.enclosures[3].animals.add(Card.Animal(AnimalType.Panda))
        currentGame.currentPlayer.zoo.enclosures[3].animals.add(Card.Animal(AnimalType.Panda))
        assertTrue(ki.thatHasThisAnimal(Card.Animal(AnimalType.Panda,Maturity.Male)
            ,currentGame.currentPlayer.zoo.enclosures).size==1)
        // test for has  space new animal
        assertTrue(ki.hasEmptySpaceForNewAnimal(currentGame.currentPlayer.zoo.enclosures))
        currentGame.currentPlayer.zoo.enclosures[2].animals.add(Card.Animal(AnimalType.Leopard))
        currentGame.currentPlayer.zoo.enclosures[1].animals.add(Card.Animal(AnimalType.Zebra))
        assertFalse(ki.hasEmptySpaceForNewAnimal(currentGame.currentPlayer.zoo.enclosures))
        currentGame.currentPlayer.zoo.enclosures[1].animals= mutableListOf(null,null,null)

        //test for is tradeable
        nextPlayer(currentGame,1).zoo.enclosures[3].animals.add(Card.Animal(AnimalType.Leopard))
        nextPlayer(currentGame,1).zoo.enclosures[3].animals.add(Card.Animal(AnimalType.Leopard))
        nextPlayer(currentGame,1).zoo.enclosures[2].animals.add(Card.Animal(AnimalType.Zebra))
        assertTrue(ki.isTradableAnimal(Card.Animal(AnimalType.Leopard),currentGame,currentGame.currentPlayer))
        assertFalse(ki.isTradableAnimal(Card.Animal(AnimalType.Panda),currentGame,currentGame.currentPlayer))

        // test has emty space for shop
        assertTrue(ki.hasEmptySpaceForShop(currentGame.currentPlayer.zoo.enclosures))
        var type =1
        currentGame.currentPlayer.zoo.enclosures.forEach {
            if(!it.isBarn){
                it.shops.add(Card.Shop(type++))
            }
        }
        assertFalse (ki.hasEmptySpaceForShop(currentGame.currentPlayer.zoo.enclosures))

        // test for has this shop
        assertTrue(ki.hasThisShop(Card.Shop(1),currentGame.currentPlayer.zoo.enclosures))
        assertFalse(ki.hasThisShop(Card.Shop(5),currentGame.currentPlayer.zoo.enclosures))

        // test for best truck score according to the set score strategie!!
        nextPlayer(currentGame,2).zoo.enclosures[3].animals.add(Card.Animal(AnimalType.Flamingo))
        nextPlayer(currentGame,2).zoo.enclosures[3].animals.add(Card.Animal(AnimalType.Flamingo))
        nextPlayer(currentGame,2).zoo.enclosures[3].shops.add(Card.Shop(2))
        val trucks = currentGame.trucks
        trucks[0].cards.add(Card.Animal(AnimalType.Panda))
        trucks[0].cards.add(Card.Animal(AnimalType.Flamingo))
        trucks[1].cards.add(Card.Animal(AnimalType.Leopard))
        trucks[1].cards.add(Card.Shop(2))
        trucks[2].cards.add(Card.Coin)
        trucks[2].cards.add(Card.Animal(AnimalType.Flamingo))

        assertEquals(ki.bestTruckNScore(currentGame,currentGame.currentPlayer).second, trucks[0])
        currentGame.currentPlayer=nextPlayer(currentGame,1)

        assertEquals(ki.bestTruckNScore(currentGame,currentGame.currentPlayer).second, trucks[2])
        currentGame.currentPlayer=nextPlayer(currentGame,1)

        assertEquals(ki.bestTruckNScore(currentGame,currentGame.currentPlayer).second, trucks[2])
        currentGame.currentPlayer=nextPlayer(currentGame,1)

        //test for all has truck
        assertFalse(ki.allHasTruck(currentGame))

        // test for draw and place
        currentGame.deck.add(Card.Coin)
        val cardsCount =currentGame.deck.size
        ki.drawNPlace(currentGame,Card.Coin,currentGame.trucks[0])
        assertTrue(currentGame.trucks[0].cards.contains(Card.Coin))
        assertTrue(currentGame.deck.size < cardsCount)

        // test for take truck
        ki.takeTruck(currentGame.trucks.first(),currentGame)
        assertTrue(currentGame.deck.size < cardsCount)

        // test for place tiles map
        currentGame.currentPlayer.zoo.enclosures[1].animals.removeFirst()
        currentGame.currentPlayer.zoo.enclosures[1].animals.add(0,Card.Animal(AnimalType.Zebra))
        val tilesMap = ki.placeTilesMap(requireNotNull
            (currentGame.currentPlayer.selectedTruck),currentGame)
        tilesMap.forEach {
            assertTrue { it.second==currentGame.currentPlayer.zoo.enclosures[3] ||
                it.second==currentGame.currentPlayer.zoo.enclosures[0]}
        }
     // test for money action
     //first player : the player should expand
     currentGame.currentPlayer.balance++
     currentGame.currentPlayer.zoo.enclosures[0].animals.add(Card.Animal(AnimalType.Panda))
     nextPlayer(currentGame,1).zoo.enclosures[0].animals.add(Card.Animal(AnimalType.Panda))
     nextPlayer(currentGame,2).zoo.enclosures[0].animals.add(Card.Animal(AnimalType.Leopard))
     assertEquals(ki.phaseTwo(currentGame,currentGame.currentPlayer).first, KIServiceV2.Action.Move1Token)

     //first player should purchase 1 token
        currentGame.currentPlayer.zoo.barn.animals= mutableListOf()
     currentGame.currentPlayer.zoo.enclosures[1].animals.add(Card.Animal(AnimalType.Zebra))
     currentGame.currentPlayer.zoo.enclosures[1].animals.add(Card.Animal(AnimalType.Zebra))
        assertEquals(ki.phaseTwo(currentGame,currentGame.currentPlayer).first, KIServiceV2.Action.Purchase1Token)
     }

    /**
     * here a game with 3 players is simulated for 50 rounds and checks
     * if anything goes wrong(if any exceptions or errors are thrown back)
     * note that game behaviour can be checked manually (if it's done under the set rules of the zooleretto game)
     * by setting a break point in line 135 and start debugging :)
     */
    @Test
    fun pleaseWork(){
        val rootService = RootService()
        val currentGame =rootService.gameMaster.currentGame
        val ki =rootService.kiServiceV2
        rootService.playerActionService.game=currentGame
        initializeGameMaster(currentGame,3)
        var returnTrucksafter  = currentGame.players.size
        var boolean =false
        for(i in 1..10) {
             val turn =ki.getBestTurn(currentGame)
            when(turn){
                is Turn.MoneyAction->rootService.playerActionService.moneyAction(turn.m,currentGame,currentGame.currentPlayer)
                else -> rootService.playerActionService.turn(turn,currentGame)
            }
            if (ki.allHasTruck(currentGame)) {
                boolean=true
            }
            if(boolean && returnTrucksafter==0){
                currentGame.players.forEach {
                    currentGame.trucks.add(it.selectedTruck!!)
                    it.selectedTruck = null
                }
                boolean=false
                returnTrucksafter=currentGame.players.size
            }
            if(boolean){
                returnTrucksafter--
            }
            currentGame.currentPlayer=ki.nextPlayer(currentGame.currentPlayer,currentGame)
            while (!ki.allHasTruck(currentGame)&&currentGame.currentPlayer.selectedTruck != null)
                currentGame.currentPlayer=ki.nextPlayer(currentGame.currentPlayer,currentGame)
        }
        // if it reaches here than the test is done :)
        assertTrue(true)
    }

    /**
     * initialize the trucks
     * @param game current game
     */
    private fun initializeTrucks(game : ZoolorettoGame){
        game.trucks.removeAll { true }
        var capacity = 1
        repeat(game.players.size){
            if (game.players.size==2) {
                game.trucks.add(Truck(mutableListOf(),capacity++))
            }
            else game.trucks.add(Truck(mutableListOf(null,null,null),3))
        }
        if(game.players.size==2) game.trucks.add(Truck(mutableListOf(),capacity))
    }

    /**
     * initialize the zoos
     * @param game current game
     */
    private fun initializeZoos(game : ZoolorettoGame ){
        for (player in game.players){
            player.zoo= Zoo(game.players.size)
            player.balance = 2
            if(player ==game.currentPlayer)game.currentPlayer.zoo.barn=player.zoo.barn
        }
    }

    /**
     * initialize players
     * @param game current game
     */
    private fun initializePlayers(game : ZoolorettoGame , playerCount:Int){
        val players =listOf<Player>( Player.AIPlayer("AI1",AIDifficulty.EASY),Player.AIPlayer("AI2",AIDifficulty.EASY),
            Player.AIPlayer("AI3",AIDifficulty.EASY),Player.AIPlayer("AI4",AIDifficulty.EASY),Player.AIPlayer("AI5",AIDifficulty.EASY))
        repeat(playerCount) {
            game.players = players.subList(0,playerCount)
        }
        game.currentPlayer=game.players.first()

    }

    /**
     * initialize game master
     * @param game current game
     * @param playerCount how many player should be in this game
     */
    private fun initializeGameMaster(game:ZoolorettoGame,playerCount: Int){
        initializePlayers(game,playerCount)
        initializeZoos(game)
        initializeTrucks(game)
        game.bankCoins=30
    }

    /**
     * returns next player
     * @param game current game
     * @param nextPosition position of the next player
     */
    private fun nextPlayer(game:ZoolorettoGame,nextPosition:Int):Player{
        val playerNumber=game.players.size
        val playerIndex = game.players.indexOf(game.currentPlayer)
        return game.players[(playerIndex+nextPosition)%playerNumber]
    }
}