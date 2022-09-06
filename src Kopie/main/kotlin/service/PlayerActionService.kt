package service

import entity.*

/**
 * This class contains the deep turn mechanics
 */
class PlayerActionService : AbstractRefreshingService() {
    lateinit var game: ZoolorettoGame

    /**
     * Executes a specific turn.
     *
     * @param t the turn to execute
     * @param currentGame the gameState this turn should be executed on
     */
    fun turn(t: Turn, currentGame: ZoolorettoGame = game) {
        when (t) {
            is Turn.Draw -> {
                t.truck.cards[t.index] = when (currentGame.deck.size) {
                    0 -> currentGame.extraCards
                    else -> currentGame.deck
                }.removeFirst()
                currentGame.drawnCard = t.truck.cards[t.index]
            }
            is Turn.MoneyAction -> moneyAction(t.m)
            is Turn.TakeTruck -> {
                require(t.truck in currentGame.trucks)
                currentGame.currentPlayer.selectedTruck = t.truck
                val index = currentGame.trucks.indexOf(t.truck)
                currentGame.trucks[index] = Truck(capacity = t.truck.capacity, aiConsider = false)
                currentGame.trucks[index].cards = listOf(null, null, null).toMutableList()
                t.truck.cards = MutableList(3) { null }
                for (entry in t.map.filterNot { it.first is Card.Coin }) {
                    addCard(
                        card = entry.first,
                        enclosure = entry.second!!,
                        index = entry.third,
                        currentGame = currentGame
                    )
                }
                val coins = t.map.filter { it.first is Card.Coin }.size
                currentGame.currentPlayer.balance += coins.coerceAtMost(currentGame.bankCoins)
                currentGame.bankCoins -= coins.coerceAtMost(currentGame.bankCoins)
            }
        }
    }

    /**
     * The cost of a MoneyAction
     *
     * @param m the MoneyAction to evaluate
     */
    fun moneyActionCost(m: MoneyAction) = when (m) {
        is MoneyAction.Move1Token -> 1
        is MoneyAction.Swap2Types -> 1
        is MoneyAction.Purchase1Token -> 2
        is MoneyAction.Discard1Token -> 2
        is MoneyAction.ExpandZoo -> 3
        else -> 0
    }

    /**
     * executes a moneyactio on the current game
     */
    fun moneyAction(m: MoneyAction, currentGame: ZoolorettoGame = game, player: Player = currentGame.currentPlayer) {
        player.balance -= moneyActionCost(m)
        currentGame.bankCoins += moneyActionCost(m)

        when (m) {
            is MoneyAction.Move1Token -> {
                if (m.takeFromLocation == null) {
                    addCard(m.moveLocation, m.chosenCard, m.index, currentGame)
                    val enclosureFrom = if (m.chosenCard is Card.Animal) {
                        currentGame.currentPlayer.zoo.enclosures.filter {
                            it.animals.contains(m.chosenCard) && it != m.moveLocation
                        }
                    } else {
                        currentGame.currentPlayer.zoo.enclosures.filter {
                            it.shops.contains(m.chosenCard as Card.Shop) &&
                                    it != m.moveLocation
                        }
                    }[0]
                    removeCard(enclosureFrom, m.chosenCard)
                } else {
                    addCard(m.moveLocation, m.chosenCard, m.index, currentGame)
                    removeCard(m.takeFromLocation, m.chosenCard)
                }
            }
            is MoneyAction.Swap2Types -> {
                val enc1 = m.map.keys.toList()[0]
                val enc2 = m.map.keys.toList()[1]
                val keep1 = enc1.animals.filterNotNull().filterNot { it.animalType == m.map[enc1] }
                val wantedFor1 = enc2.animals.filterNotNull().filter { it.animalType == m.map[enc2] }
                val keep2 = enc2.animals.filterNotNull().filterNot { it.animalType == m.map[enc2] }
                val wantedFor2 = enc1.animals.filterNotNull().filter { it.animalType == m.map[enc1] }
                enc2.animals = (keep2 + wantedFor2).toMutableList()
                enc2.animals = (enc2.animals + List(enc2.animalCapacity - enc2.animals.size) { null }).toMutableList()
                enc1.animals = (keep1 + wantedFor1).toMutableList()
                enc1.animals = (enc1.animals + List(enc1.animalCapacity - enc1.animals.size) { null }).toMutableList()


                if(currentGame.currentPlayer.zoo.barn!=enc2){
                    var enc2HornyMales = enc2.animals.filterNotNull().filter{ (it.maturity == Maturity.Male) && it.isFertile }
                    var enc2HornyFemales = enc2.animals.filterNotNull().filter{ (it.maturity == Maturity.Female) && it.isFertile }

                    while(enc2HornyFemales.isNotEmpty() && enc2HornyMales.isNotEmpty()) {
                        val hornyMale = enc2HornyMales[0]
                        val hornyFemale = enc2HornyFemales[0]
                        hornyMale.isFertile = false
                        hornyFemale.isFertile = false

                        val child = Card.Animal(hornyMale.animalType, Maturity.OffSpring, false)
                        if (enc2.animalCapacity > enc2.animals.filterNotNull().size) {
                            enc2.add(child)
                        } else {
                            currentGame.currentPlayer.zoo.barn.add(child)
                        }

                        enc2HornyMales = enc2.animals.filterNotNull().filter{ (it.maturity == Maturity.Male) && it.isFertile }
                        enc2HornyFemales = enc2.animals.filterNotNull().filter{ (it.maturity == Maturity.Female) && it.isFertile }

                    }
                }

                if(currentGame.currentPlayer.zoo.barn!=enc1){
                    var enc1HornyMales = enc1.animals.filterNotNull().filter{ (it.maturity == Maturity.Male) && it.isFertile }
                    var enc1HornyFemales = enc1.animals.filterNotNull().filter{ (it.maturity == Maturity.Female) && it.isFertile }

                    while(enc1HornyFemales.isNotEmpty() && enc1HornyMales.isNotEmpty()) {
                        val hornyMale = enc1HornyMales[0]
                        val hornyFemale = enc1HornyFemales[0]
                        hornyMale.isFertile = false
                        hornyFemale.isFertile = false

                        val child = Card.Animal(hornyMale.animalType, Maturity.OffSpring, false)
                        if (enc1.animalCapacity > enc1.animals.filterNotNull().size) {
                            enc1.add(child)
                        } else {
                            currentGame.currentPlayer.zoo.barn.add(child)
                        }

                        enc1HornyMales = enc1.animals.filterNotNull().filter{ (it.maturity == Maturity.Male) && it.isFertile }
                        enc1HornyFemales = enc1.animals.filterNotNull().filter{ (it.maturity == Maturity.Female) && it.isFertile }

                    }
                }

            }
            is MoneyAction.Purchase1Token -> {
                currentGame.bankCoins -= 1 // Because we added one too much at the start of this function
                m.sPlayer.balance += 1
                addCard(m.moveLocation, m.chosenAnimal, m.index, currentGame)
                removeCard(m.sPlayer.zoo.barn, m.chosenAnimal)
            }
            is MoneyAction.Discard1Token -> removeCard(player.zoo.barn, m.chosenCard)
            is MoneyAction.ExpandZoo -> player.zoo.expand()
            else -> {}
        }
    }

    private fun addCard(enclosure: Enclosure, card: Card, index: Int, currentGame: ZoolorettoGame = game) {
        when (card) {
            is Card.Animal -> {
                if(enclosure.isBarn) enclosure.add(card)
                else enclosure.animals[index] = card
                if (card.isFertile) {
                    val otherSex = if (card.maturity == Maturity.Male) Maturity.Female else Maturity.Male
                    val possiblePartners =
                        enclosure.animals.filterNotNull()
                            .filter { it.isFertile && it.animalType == card.animalType && it.maturity == otherSex }
                    if (currentGame.currentPlayer.zoo.barn!=enclosure && possiblePartners.isNotEmpty()) {
                        val partner = possiblePartners[0]
                        card.isFertile = false
                        partner.isFertile = false
                        val child = Card.Animal(partner.animalType, Maturity.OffSpring, false)
                        if (enclosure.animalCapacity - enclosure.animals.filterNotNull().size > 0) {
                            enclosure.add(child)
                        } else {
                            currentGame.currentPlayer.zoo.barn.add(child)
                        }
                    }
                }
                if (!enclosure.isBarn && enclosure.animals.filterNotNull().size == enclosure.animalCapacity
                    && currentGame.bankCoins > 0) {
                    currentGame.currentPlayer.balance += enclosure.coinsFull.coerceAtMost(currentGame.bankCoins)
                    currentGame.bankCoins -= enclosure.coinsFull.coerceAtMost(currentGame.bankCoins)
                }
            }
            is Card.Shop -> {
                if (index == enclosure.shops.size) {
                    enclosure.shops.add(card)
                } else {
                    enclosure.shops[index] = card
                }
            }
            else -> {}
        }
    }

    private fun removeCard(enclosure: Enclosure, card: Card) {
        when (card) {
            is Card.Animal -> enclosure.animals[enclosure.animals.indexOf(card)] = null
            is Card.Shop -> enclosure.shops[enclosure.shops.indexOf(card)] = null
            else -> {}
        }
    }


}
