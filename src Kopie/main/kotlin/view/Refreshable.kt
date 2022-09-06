package view

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 *
 */
interface Refreshable {

    /**
     * perform refreshes that are necessary after a new game started.
     */
    fun refreshAfterStartNewGame() {}

    /**
     * Performs refreshes and gets called when the turn is over.
     */
    fun refreshAfterTurn() {}

    /**
     * Refresh after round end
     */
    fun refreshAfterRoundEnd() {}


    /**
     * Refresh after the game has ended
     */
    fun refreshAfterGameEnd() {}

}
