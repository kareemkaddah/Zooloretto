package service
import entity.*
import kotlin.test.*

/**
 * Test the [PlayerActionService]
 */
class PlayerActionServiceTest {
    //Creates Root Service with ready to test dummy current game.
    private var rootService: RootService = RootService()
    private val dummyPlayers = listOf("p1", "p2", "p3").map{ Player.HumanPlayer(it) }
    private val dummyPlayers2 = listOf("p1", "p2").map{ Player.HumanPlayer(it) }
    // GAME JUST HAS THE FIRST GAME DONT USE FOR MORE THAN 1 TURN
    private lateinit var game : ZoolorettoGame

    /**
     * Creates a dummy Game before every test
     */
    @BeforeTest
    fun makeDummyGame() {
        rootService.gameMaster = GameMaster(ZoolorettoGame(dummyPlayers))
        game = requireNotNull(rootService.gameMaster.currentGame)
        game.currentPlayer.balance = 100
        rootService.playerActionService.game=game
    }
    
    /**
     * Tests general Turn Stuff
     */
    @Test
    fun turn(){
        val prePlayer = game.currentPlayer
        rootService.gameService.turn(Turn.Draw(game.trucks[1],0))
        assertNotEquals(prePlayer,game.currentPlayer)
        println(game.currentPlayer)
        rootService.gameService.turn(Turn.TakeTruck(game.trucks[1], listOf(Triple(game.trucks[1].cards[0]!!,game.currentPlayer.zoo.enclosures[1],0))),game)
        assertNotEquals(prePlayer,game.currentPlayer)
        println(game.currentPlayer)
    }

    /**
     * Tests the turn Draw
     */
    @Test
    fun draw(){
        val truck = game.trucks[1]
        val index = 2
        val preCardsInDeck : Int = game.deck.size
        rootService.gameService.turn(Turn.Draw(truck,index))
        assertEquals(preCardsInDeck-1,game.deck.size)
        assertNotNull(truck.cards[index])
        assertEquals("p1 hat eine Karte gezogen.",game.playedTurns[0])
    }

    /**
     * Tests the MoneyAction Move1Token
     */
    @Test
    fun move1Token(){
        game.currentPlayer.zoo.enclosures[0].animals[2] = Card.Animal(AnimalType.Elefant)
        val player = game.currentPlayer
        rootService.gameService.turn(Turn.MoneyAction(MoneyAction.Move1Token(
            game.currentPlayer.zoo.enclosures[1],
            game.currentPlayer.zoo.enclosures[0].animals[2]!!,
            2
        )))
        assertEquals(Card.Animal(AnimalType.Elefant),player.zoo.enclosures[1].animals[2])
        assertEquals("p1 hat eine Karte in seinem Zoo bewegt.",game.playedTurns[0])
    }

    /**
     * last Test for shop
     */
    @Test
    fun move1TokenShop(){
        game.currentPlayer.zoo.enclosures[0].shops[0] = Card.Shop(1)
        val player = game.currentPlayer
        rootService.gameService.turn(Turn.MoneyAction(MoneyAction.Move1Token(
            game.currentPlayer.zoo.enclosures[1],
            game.currentPlayer.zoo.enclosures[0].shops[0]!!,
            0
        )))
        assertEquals(Card.Shop(1),player.zoo.enclosures[1].shops[0])
    }
    /**
     * Tests the MoneyAction Swap2Types
     */
    @Test
    fun swap2Types(){
        game.currentPlayer.zoo.enclosures[0].animals[1] = Card.Animal(AnimalType.Elefant)
        game.currentPlayer.zoo.enclosures[0].animals[2] = Card.Animal(AnimalType.Panda)
        game.currentPlayer.zoo.enclosures[0].animals[3] = Card.Animal(AnimalType.Kamel)
        game.currentPlayer.zoo.enclosures[0].shops[0] = Card.Shop(5)
        game.currentPlayer.zoo.enclosures[1].animals[2] = Card.Animal(AnimalType.Kaenguru)
        val player = game.currentPlayer
        rootService.gameService.turn(Turn.MoneyAction(MoneyAction.Swap2Types(
            mapOf(
                game.currentPlayer.zoo.enclosures[0] to AnimalType.Elefant,
                game.currentPlayer.zoo.enclosures[1] to AnimalType.Kaenguru
            )
        )))
        assertTrue(player.zoo.enclosures[1].animals.contains(Card.Animal(AnimalType.Elefant)))
        assertTrue(player.zoo.enclosures[0].animals.contains(Card.Animal(AnimalType.Kaenguru)))
        assertTrue(player.zoo.enclosures[0].animals.contains(Card.Animal(AnimalType.Kamel)))
        assertTrue(player.zoo.enclosures[0].animals.contains(Card.Animal(AnimalType.Panda)))
        assertTrue(player.zoo.enclosures[0].shops.contains(Card.Shop(5)))
        assertEquals("p1 hat zwei Tierarten getauscht.",game.playedTurns[0])
    }

    /**
    * Tests the MoneyAction Purchase1Token
    */
    @Test
    fun purchase1Token(){
        val player = game.currentPlayer
        val otherPlayer = game.players[1]
        val prePlayerBalance = otherPlayer.balance
        otherPlayer.zoo.barn.animals[0] = Card.Animal(AnimalType.Kamel)
        rootService.gameService.turn(Turn.MoneyAction(MoneyAction.Purchase1Token(
            moveLocation = player.zoo.enclosures[2],
            sPlayer = otherPlayer,
            chosenAnimal = otherPlayer.zoo.barn.animals[0]!!,
            2
        )))
        assertEquals(player.zoo.enclosures[2].animals[2], Card.Animal(AnimalType.Kamel))
        assertFalse(otherPlayer.zoo.barn.animals.contains(Card.Animal(AnimalType.Kamel)))
        assertEquals(prePlayerBalance+1,otherPlayer.balance)
        assertEquals("p1 hat eine Karte gekauft.",game.playedTurns[0])
    }

    /**
     * Tests the MoneyAction Discard1Token
     */
    @Test
    fun discard1Token(){
        val player = game.currentPlayer
        player.zoo.barn.animals[0] = Card.Animal(AnimalType.Kamel)
        rootService.gameService.turn(Turn.MoneyAction(MoneyAction.Discard1Token(player.zoo.barn.animals[0]!!)))
        assertFalse(player.zoo.barn.animals.contains(Card.Animal(AnimalType.Kamel)))
        assertEquals("p1 hat eine Karte entsogt.",game.playedTurns[0])
    }

    /**
     * Tests the MoneyAction ExpandZoo
     */
    @Test
    fun expandZoo(){
        val player = game.currentPlayer
        rootService.gameService.turn(Turn.MoneyAction(MoneyAction.ExpandZoo))
        assertEquals(0,player.zoo.expanded)
        assertEquals(97, player.balance)
        assertEquals("p1 hat seinen Zoo erweitert.",game.playedTurns[0])
    }

    /**
     * Tests ExpandZoo for a 2-player game. Currently not working.
     */


    @Test
    fun expandZooTwoPlayers(){
        rootService.gameMaster = GameMaster(ZoolorettoGame(dummyPlayers2))
        rootService.gameMaster.currentGame.currentPlayer.balance = 100
        assertEquals(2, rootService.gameMaster.currentGame.currentPlayer.zoo.expanded)
        assertEquals(100, rootService.gameMaster.currentGame.currentPlayer.balance)
        for(i in 0..1){
            rootService.gameService.turn(Turn.MoneyAction(MoneyAction.ExpandZoo))
            rootService.gameService.turn(Turn.Draw(game.trucks[0],i))
        }
        assertEquals(0, rootService.gameMaster.currentGame.players[0].zoo.expanded)
        assertEquals(94, rootService.gameMaster.currentGame.players[0].balance)
        assertFails{ rootService.gameService.turn(Turn.MoneyAction(MoneyAction.ExpandZoo)) }
        assertEquals(
            "[p1 hat seinen Zoo erweitert., p2 hat eine Karte gezogen.," +
                    " p1 hat seinen Zoo erweitert., p2 hat eine Karte gezogen.]",
            rootService.gameMaster.currentGame.playedTurns.toString()
        )
    }


    @Test
    fun testPrepare(){
        for(i in 0..2){
            rootService.gameService.turn(Turn.Draw(game.trucks[i],0))
            rootService.gameService.turn(Turn.Draw(game.trucks[i],1))
            rootService.gameService.turn(Turn.Draw(game.trucks[i],2))
        }
    }


    /**
     * Tests the turn takeTruck
     */
    @Test
    fun takeTruck(){
        val truck = game.trucks[1]
        truck.cards[0] = Card.Animal()
        truck.cards[1] = Card.Shop(2)
        truck.cards[2] = Card.Coin
        truck.cards.forEach{assertNotNull(it)}

        val truck1 = game.trucks[0]
        truck1.cards[0] = Card.Shop(1)
        truck1.cards[1] = Card.Animal()
        truck1.cards[2] = Card.Coin
        truck1.cards.forEach{ assertNotNull(it)}
        val enc1 = game.currentPlayer.zoo.enclosures[1]
        val enc2 = game.currentPlayer.zoo.enclosures[3]

        assertContains(game.trucks, truck1)
        println(game.trucks.indexOf(truck1))
        println(game.trucks.indexOf(truck))

        val map: List<Triple<Card,Enclosure?,Int>> = listOf(
            Triple(truck.cards[0]!!,enc1,0),
            Triple(truck.cards[1]!!,enc2,1)
        )

        val map1: List<Triple<Card,Enclosure?,Int>> = listOf(
            Triple(truck1.cards[1]!!,game.currentPlayer.zoo.enclosures[2],0),
            Triple(truck1.cards[0]!!,game.currentPlayer.zoo.enclosures[2],1)
        )

        val player = game.currentPlayer
        val preCoins = player.balance
        val truckCards : List<Card> = truck.cards.filterNotNull()
        rootService.gameService.turn(Turn.TakeTruck(truck ,map),game)

        assertNotNull(player.selectedTruck)
        truck.cards.forEach{assertNull(it)}

        assertEquals(truckCards[0],player.zoo.enclosures[1].animals.filterNotNull()[0])
        assertEquals(truckCards[1],player.zoo.enclosures[3].shops.filterNotNull()[0])
        assertEquals(preCoins,player.balance)

        val truckCards1 : List<Card> = truck1.cards.filterNotNull()
        println(game.currentPlayer.selectedTruck)
        assertContains(game.trucks, truck1)
        println(game.trucks.indexOf(truck1))
        println(game.trucks.indexOf(truck))
        rootService.gameService.turn(Turn.TakeTruck(truck1 , map1), game)

        assertNotNull(player.selectedTruck)
        truck1.cards.forEach{assertNull(it)}

        //assertEquals(truckCards1[0],player.zoo.barn.animals.filterNotNull()[0])
        //assertEquals(preCoins,player.balance)

    }





}