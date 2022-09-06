package view

import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*

/**
 * This class provides some extra features for the GUI like showing tooltips.
 * This also provides a good foundation for future work on other features.
 */
class ImprovedUI {

    /**
     * Represents the bubble that contains tooltip messages in a certain scene.
     */
    lateinit var tooltipViewer: Label

    /**
     * Top 2 layers are always used for tooltip.
     * @param container is the [Label] bubble that contains the tooltip.
     * @param component is the [UIComponent] to which the tooltip refers.
     * @param message is the text inside the tooltip bubble.
     */
    fun showTooltipVisual(
        component: UIComponent,
        message: String = "",
    ): CompoundVisual {
        val w = maxOf(100, (message.length * 10)).toDouble()
        val result = CompoundVisual()
        val layers = mutableListOf<SingleLayerVisual>()
        layers.addAll(listOf(
            ImageVisual("images/Bubble.png"),
            TextVisual(text = message, font = Font(size = 18))
        ))
        result.children = layers.toList()
        tooltipViewer.posX =  component.posX + (component.width - w)/2 //component.posX + w/2
        /* Make sure bubble does not go beyond screen borders */
        tooltipViewer.posY = if(component.posY >= 50) {
            component.posY - 60
        } else {
            component.posY + component.height + 10
        }
        tooltipViewer.width = w
        tooltipViewer.isVisible = true

        /* Move tooltips to front */

        return result
    }

    /**
     * Removes/hides tooltip.
     * @param container is the bubble that contains the tooltip message.
     */
    fun hideTooltip() {
        tooltipViewer.isVisible = false
    }
}