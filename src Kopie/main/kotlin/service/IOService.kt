package service

import entity.GameMaster
import entity.Player
import java.io.*

/**
 * Class for saving/loading a game from/to a file
 */
class IOService(var rootService: RootService) {

    /**
     * Saves the playerlist
     */
    fun saveHighscorePlayerToFile(fileName: String, playerList: List<Player>) {
        val a = FileOutputStream(fileName)
        val b = ObjectOutputStream(a)
        b.writeObject(playerList)
        b.close()
    }

    /**
     * Loads a playerlist
     */
    fun loadHighscorePlayer(fileName: String):List<Player> {
        try {
            val a = FileInputStream(fileName)
            val b = ByteArrayInputStream(a.readBytes())
            val c = ObjectInputStream(b)
            @Suppress("unchecked_cast")
            val players = (c.readObject() as List<Player>)
            c.close()
            return players
        }catch (e: Exception){
            return emptyList()
        }
    }

    /**
     * Saves a game
     */
    fun saveGameToFile(fileName: String) {
        val a = FileOutputStream(fileName)
        val b = ObjectOutputStream(a)
        b.writeObject(rootService.gameMaster)
        b.close()
    }

    /**
     * Loads a game from a file
     */
    fun loadGameFromFile(fileName: String) {
        val a = FileInputStream(fileName)
        val b = ByteArrayInputStream(a.readBytes())
        val c = ObjectInputStream(b)
        rootService.gameMaster = (c.readObject() as GameMaster)
        c.close()
    }



}