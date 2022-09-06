package entity

import java.io.*
import java.util.*


/**
 * Game-master.
 *
 * @param currentGame the currentGame
 * @param turns the list of gameStates that are saved for undo and redo functionality
 */
class GameMaster(
    var currentGame: ZoolorettoGame,
    var turns: MutableList<ZoolorettoGame> = mutableListOf(currentGame)
) : Serializable {
    var currentGameIndex : Int = 0

    /**
     * Appends a new game to the list of gameStates
     *
     * @return The new currentGame
     */
    fun appendGame() {
         var copiedGame: ZoolorettoGame = deepCopy(turns.last())
         turns.add(copiedGame)
         currentGame = turns.last()
         currentGameIndex++
      }

    /**
     * Undo's a turn
     */
    fun undo() {
        currentGameIndex = (currentGameIndex-1).coerceAtLeast(0)
        currentGame = turns[ currentGameIndex ]
    }

    /**
     * Redo's a turn
     */
    fun redo() {
        currentGameIndex = (currentGameIndex+1).coerceAtMost(turns.size-1)
        currentGame = turns[currentGameIndex]
    }

    /**
     * takes the Player name and score from highScore.txt and converts them to a list.
     */
    fun readHighScores():ArrayList<Pair<String,Int>>{
        val s = Scanner(File("highScore.txt"))
        val playersName = ArrayList<String>()
        val playersScore = ArrayList<Int>()
        while (s.hasNext()) {
            playersName.add(s.next())
            if(s.hasNext()){
                playersScore.add(Integer.parseInt(s.next()))
            }
        }
        s.close()
        val savedHighScores: ArrayList<Pair<String,Int>> = ArrayList<Pair<String,Int>>()

        for (i in 1..playersName.size) {
            savedHighScores.add(Pair(playersName[i],playersScore[i]))
        }

        return savedHighScores


    }
    /**
     * saves the playerName and Score from the current game in highScore.txt
     */
    fun saveHighScore(){

        val savedHighScores=readHighScores();


        val writer = FileWriter("highScore.txt")
        //TODO read and check old scores
        for (score in currentGame.gameScore) {
            writer.write(score.first.getPlayerName())
            writer.write(score.second.toString())
        }
        writer.close()
    }

    /**
     * Makes a gameState serializable
     */
    companion object {
        /**
         * Makes a deepcopy of the gamestate
         *
         * @param obj Game of which the deepcopy is made
         * @return deepcopy of the game
         */
        fun <T : Serializable> deepCopy(obj: ZoolorettoGame): T {
            val baos = ByteArrayOutputStream()
            val oos  = ObjectOutputStream(baos)
            oos.writeObject(obj)
            oos.close()
            val bais = ByteArrayInputStream(baos.toByteArray())
            val ois  = ObjectInputStream(bais)
            @Suppress("unchecked_cast")
            return ois.readObject() as T
        }
    }

}

