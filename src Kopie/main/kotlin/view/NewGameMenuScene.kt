package view

import entity.AIDifficulty
import entity.Player
import service.RootService
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.dialog.*
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import java.awt.Color
import java.io.File

/**
 * This is the main menu screen. Players list sizing 2 - 5 players will be constructed and
 * forwarded from here to [ZoolorettoGameScene].
 * @param app is the [ZoolorettoGameScene] that allows showing dialog popups.
 * @param rootService is the core that holds every part of this project connected.
 */
class NewGameMenuScene(private val app: BoardGameApplication, private val rootService: RootService) :
    MenuScene(1920, 1080), Refreshable {

    /**F
     * Used to store read file for card presets.
     */
    private var cardsFile: File? = null

    /**
     * UI improver.
     */
    private val improvedUI: ImprovedUI = ImprovedUI()
    private val tooltipViewer: Label = Label(
        height = 50, visual = CompoundVisual(ImageVisual("images/Bubble.png"))
    ).apply {
        isVisible = false
        isFocusable = false
    }

    /**
     * Used for adding AI players
     */
    private val playerFields: BidirectionalMap<TextField, ComboBox<String>> = BidirectionalMap()

    /**
     * Game specification: Order of players can be randomized.
     * @see [shuffleToggle]
     */
    private var shufflePlayers = false

    /**
     * Global font.
     */
    private val globalFont: Font = Font(
        size = 36, color = Color(252, 215, 3),
        family = "Pokemon Solid"
    )

    /**
     * CSS for JavaFX. Used for styling the text fields.
     */
    private val style: String = (
        "-fx-background-color: rgba(0, 0, 0, 0.0);" +
        "-fx-background-image: url('images/TextInputField.png');" +
        "-fx-background-size: cover;" +
        "-fx-alignment: center;"
    )

    /**
     * Great personalities.
     */
    private var namesList = mutableListOf(
        "Nick", "Ahmad", "Ali", "Kareem", "Florian",
        "Jan", "Christos", "Ibrahim", "Nils", "Lasse"
    )
    

    private val namesListOrig = mutableListOf(
        "Nick", "Ahmad", "Ali", "Kareem", "Florian",
        "Jan", "Christos", "Ibrahim", "Nils", "Lasse"
    )




            /*

        .also{takenName.add(it);if(namesList.size<2){namesList=namesListOrig}}

    namesList.random().also{namesList.remove(it)}

    namesList.filterNot{takenName.contains(it)}.random().also{takenName.add(it)}
    
             */



    private val takenName = mutableListOf<String>()

    /**
     * AI names.
     */
    private val aiNamesList = mutableListOf(
        "TeslaBot", "NeutonBot", "TuringBot", "X Æ A-12", "EulerBot"
    )

    /**
     * First things first
     * @author Ahmad Jammal.
     */
    private val tuButton = Button(
        width = 100, height = 100, posX = 1800, posY = 20,
        visual = ImageVisual("images/TUButtonSmall.png")
    ).apply {
        if (!this.isDisabled) {
            onMouseEntered = {
                this.visual = ImageVisual("images/TUButtonSmallHover.png")
            }
            onMouseExited = {
                this.visual = ImageVisual("images/TUButtonSmall.png")
            }
            onMouseClicked = {
                app.showDialog(
                    Dialog(
                        dialogType = DialogType.INFORMATION,
                        title = "TU Dortmund",
                        header = "Technische Universität Dortmund",
                        message = "https://www.tu-dortmund.de/"
                    )
                )
            }
        }
    }

    private val backgroundLower = Label(
        width = 1920, height = 1080, posX = 0 , posY = 0,
        visual = ImageVisual("images/Pokéretto_Menu_Scene.png")
    ).apply { this.opacity = 0.8 }

    /**
     * Input field for player 1's name. Permanently enabled.
     */
    private val p1Input = TextField(
        width = 428, height = 80, posX = 795, posY = 340,
        text = namesList.random().also{namesList.remove(it)},
        font = globalFont,
        prompt = "Enter name",
    ).apply {
        this.componentStyle = style
        onKeyTyped = {
            countPlayers()
        }
    }

    /**
     * Input field for player 2's name. Permanently enabled.
     */
    private val p2Input = TextField(
        width = 428, height = 80, posX = 795, posY = p1Input.posY + 100,
        text = namesList.random().also{namesList.remove(it)},
        font = globalFont,
        prompt = "Enter name",
    ).apply {
        this.componentStyle = style
        onKeyTyped = {
            countPlayers()
        }
    }

    /**
     * Input field for player 1's name. Enabled upon clicking [addButton3],
     * and disabled upon clicking [removeButton3].
     */
    private val p3Input = TextField(
        width = 428, height = 80, posX = 795, posY = p2Input.posY + 100,
        text = "Add player 3",
        font = globalFont,
        prompt = "Enter name"
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        this.visual = Visual.EMPTY
        onKeyTyped = {
            countPlayers()
        }
    }

    /**
     * Input field for player 1's name. Enabled upon clicking [addButton4],
     * and disabled upon clicking [removeButton4].
     */
    private val p4Input = TextField(
        width = 428, height = 80, posX = 795, posY = p3Input.posY + 100,
        text = "Add player 4",
        font = globalFont,
        prompt = "Enter name",
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        onKeyTyped = {
            countPlayers()
        }
    }


    /**
     * Input field for player 1's name. Enabled upon clicking [addButton4],
     * and disabled upon clicking [removeButton4].
     */
    private val p5Input = TextField(
        width = 428, height = 80, posX = 795, posY = p4Input.posY + 100,
        text = "Add player 5",
        font = globalFont,
        prompt = "Enter name",
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        onKeyTyped = {
            countPlayers()
        }
    }

    private val addAI1: Button = Button(
        width = 80, height = 80, posX = 1250, posY = p1Input.posY,
        visual = ImageVisual("images/AddAIButton.png")
    ).apply {
        var isAI: Boolean
        onMouseEntered = {
            isAI = !aI1Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButtonHover.png")
                } else {
                    ImageVisual("images/HumanHover.png")
                }
            }
        }
        onMouseExited = {
            isAI = !aI1Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButton.png")
                } else {
                    ImageVisual("images/Human.png")
                }
            }
        }
        onMouseClicked = {
            isAI = !aI1Difficulty.isDisabled
            if(!this.isDisabled) {
                if(!isAI) {
                    visual = ImageVisual("images/Human.png")
                    aI1Difficulty.isDisabled = false
                    p1Input.isDisabled = true
                    p1Input.text = ""
                    p1Input.prompt = ""
                    addComponents(aI1Difficulty)
                    isAI = true
                    countPlayers()
                }
                else {
                    visual = ImageVisual("images/AddAIButton.png")
                    aI1Difficulty.isDisabled = true
                    p1Input.isDisabled = false
                    p1Input.text = namesList.random().also{namesList.remove(it)}
                    p1Input.prompt = "Enter name"
                    removeComponents(aI1Difficulty)
                    isAI = false
                    countPlayers()
                }
            }
        }
    }

    private val addAI2: Button = Button(
        width = 80, height = 80, posX = 1250, posY = p2Input.posY,
        visual = ImageVisual("images/AddAIButton.png")
    ).apply {
        var isAI: Boolean
        onMouseEntered = {
            isAI = !aI2Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButtonHover.png")
                } else {
                    ImageVisual("images/HumanHover.png")
                }
            }
        }
        onMouseExited = {
            isAI = !aI2Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButton.png")
                } else {
                    ImageVisual("images/Human.png")
                }
            }
        }
        onMouseClicked = {
            isAI = !aI2Difficulty.isDisabled
            if(!this.isDisabled) {
                if(!isAI) {
                    visual = ImageVisual("images/Human.png")
                    aI2Difficulty.isDisabled = false
                    p2Input.isDisabled = true
                    p2Input.text = ""
                    p2Input.prompt = ""
                    addComponents(aI2Difficulty)
                    isAI = true
                    countPlayers()
                }
                else {
                    visual = ImageVisual("images/AddAIButton.png")
                    aI2Difficulty.isDisabled = true
                    p2Input.isDisabled = false
                    p2Input.text = namesList.random().also{namesList.remove(it)}
                    p2Input.prompt = "Enter name"
                    removeComponents(aI2Difficulty)
                    isAI = false
                    countPlayers()
                }
            }
        }
    }

    private val addAI3: Button = Button(
        width = 80, height = 80, posX = 1250, posY = p3Input.posY,
        visual = ImageVisual("images/AddAIButtonDisabled.png")
    ).apply {
        this.isDisabled = true
        var isAI: Boolean
        onMouseEntered = {
            isAI = !aI3Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButtonHover.png")
                } else {
                    ImageVisual("images/HumanHover.png")
                }
            }
        }
        onMouseExited = {
            isAI = !aI3Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButton.png")
                } else {
                    ImageVisual("images/Human.png")
                }
            }
        }
        onMouseClicked = {
            isAI = !aI3Difficulty.isDisabled
            if(!this.isDisabled) {
                if(!isAI) {
                    visual = ImageVisual("images/Human.png")
                    aI3Difficulty.isDisabled = false
                    p3Input.isDisabled = true
                    p3Input.text = ""
                    p3Input.prompt = ""
                    addComponents(aI3Difficulty)
                    isAI = true
                    countPlayers()
                }
                else {
                    visual = ImageVisual("images/AddAIButton.png")
                    aI3Difficulty.isDisabled = true
                    p3Input.isDisabled = false
                    p3Input.text = namesList.random().also{namesList.remove(it)}
                    p3Input.prompt = "Enter name"
                    removeComponents(aI3Difficulty)
                    isAI = false
                    countPlayers()
                }
            }
        }
    }

    private val addAI4: Button = Button(
        width = 80, height = 80, posX = 1250, posY = p4Input.posY,
        visual = ImageVisual("images/AddAIButtonDisabled.png")
    ).apply {
        this.isDisabled = true
        var isAI: Boolean
        onMouseEntered = {
            isAI = !aI4Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButtonHover.png")
                } else {
                    ImageVisual("images/HumanHover.png")
                }
            }
        }
        onMouseExited = {
            isAI = !aI4Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButton.png")
                } else {
                    ImageVisual("images/Human.png")
                }
            }
        }
        onMouseClicked = {
            isAI = !aI4Difficulty.isDisabled
            if(!this.isDisabled) {
                if(!isAI) {
                    visual = ImageVisual("images/Human.png")
                    aI4Difficulty.isDisabled = false
                    p4Input.isDisabled = true
                    p4Input.text = ""
                    p4Input.prompt = ""
                    addComponents(aI4Difficulty)
                    isAI = true
                    countPlayers()
                }
                else {
                    visual = ImageVisual("images/AddAIButton.png")
                    aI4Difficulty.isDisabled = true
                    p4Input.isDisabled = false
                    p4Input.text = namesList.random().also{namesList.remove(it)}
                    p4Input.prompt = "Enter name"
                    removeComponents(aI4Difficulty)
                    isAI = false
                    countPlayers()
                }
            }
        }
    }

    private val addAI5: Button = Button(
        width = 80, height = 80, posX = 1250, posY = p5Input.posY,
        visual = ImageVisual("images/AddAIButtonDisabled.png")
    ).apply {
        this.isDisabled = true
        var isAI: Boolean
        onMouseEntered = {
            isAI = !aI5Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButtonHover.png")
                } else {
                    ImageVisual("images/HumanHover.png")
                }
            }
        }
        onMouseExited = {
            isAI = !aI5Difficulty.isDisabled
            if(!this.isDisabled) {
                visual = if(!isAI) {
                    ImageVisual("images/AddAIButton.png")
                } else {
                    ImageVisual("images/Human.png")
                }
            }
        }
        onMouseClicked = {
            isAI = !aI5Difficulty.isDisabled
            if(!this.isDisabled) {
                if(!isAI) {
                    visual = ImageVisual("images/Human.png")
                    aI5Difficulty.isDisabled = false
                    p5Input.isDisabled = true
                    p5Input.text = ""
                    p5Input.prompt = ""
                    addComponents(aI5Difficulty)
                    isAI = true
                    countPlayers()
                }
                else {
                    visual = ImageVisual("images/AddAIButton.png")
                    aI5Difficulty.isDisabled = true
                    p5Input.isDisabled = false
                    p5Input.text = namesList.random().also{namesList.remove(it)}
                    p5Input.prompt = "Enter name"
                    removeComponents(aI5Difficulty)
                    isAI = false
                    countPlayers()
                }
            }
        }
    }

    private val aI1Difficulty: ComboBox<String> = ComboBox<String>(
        width = 428, height = 80, posX = 795, posY = p1Input.posY,
        prompt = "Select difficulty", font = globalFont
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        val name = aiNamesList.removeAt((0 until aiNamesList.size).random())
        this.items = listOf("$name: ${AIDifficulty.EASY}", "$name: ${AIDifficulty.MEDIUM}", "$name: ${AIDifficulty.HARD}")
        selectedItemProperty.addListener { _, _ ->
            countPlayers()
        }
    }

    private val aI2Difficulty: ComboBox<String> = ComboBox<String>(
        width = 428, height = 80, posX = 795, posY = p2Input.posY,
        prompt = "Select difficulty", font = globalFont
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        val name = aiNamesList.removeAt((0 until aiNamesList.size).random())
        this.items = listOf("$name: ${AIDifficulty.EASY}", "$name: ${AIDifficulty.MEDIUM}", "$name: ${AIDifficulty.HARD}")
        selectedItemProperty.addListener { _, _ ->
            countPlayers()
        }
    }

    private val aI3Difficulty: ComboBox<String> = ComboBox<String>(
        width = 428, height = 80, posX = 795, posY = p3Input.posY,
        prompt = "Select difficulty", font = globalFont
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        val name = aiNamesList.removeAt((0 until aiNamesList.size).random())
        this.items = listOf("$name: ${AIDifficulty.EASY}", "$name: ${AIDifficulty.MEDIUM}", "$name: ${AIDifficulty.HARD}")
        selectedItemProperty.addListener { _, _ ->
            countPlayers()
        }
    }

    private val aI4Difficulty: ComboBox<String> = ComboBox<String>(
        width = 428, height = 80, posX = 795, posY = p4Input.posY,
        prompt = "Select difficulty", font = globalFont
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        val name = aiNamesList.removeAt((0 until aiNamesList.size).random())
        this.items = listOf("$name: ${AIDifficulty.EASY}", "$name: ${AIDifficulty.MEDIUM}", "$name: ${AIDifficulty.HARD}")
        selectedItemProperty.addListener { _, _ ->
            countPlayers()
        }
    }

    private val aI5Difficulty: ComboBox<String> = ComboBox<String>(
        width = 428, height = 80, posX = 795, posY = p5Input.posY,
        prompt = "Select difficulty", font = globalFont
    ).apply {
        this.componentStyle = style
        this.isDisabled = true
        val name = aiNamesList.removeAt((0 until aiNamesList.size).random())
        this.items = listOf("$name: ${AIDifficulty.EASY}", "$name: ${AIDifficulty.MEDIUM}", "$name: ${AIDifficulty.HARD}")
        selectedItemProperty.addListener { _, _ ->
            countPlayers()
        }
    }

    /**
     * Adds [removeButton3] and activates [p3Input] and [addButton4]. Also removes itself.
     */
    private val addButton3: Button = Button(
        width = 160, height = 80, posX = 600, posY = p3Input.posY,
        visual = ImageVisual("images/AddPlayer.png")
    ).apply {
        isDisabled = false
        visual = ImageVisual("images/AddPlayer.png")
        onMouseEntered = {
            if (!this.isDisabled) this.visual = ImageVisual("images/AddPlayerHover.png")
        }
        onMouseExited = {
            if (!this.isDisabled) this.visual = ImageVisual("images/AddPlayer.png")
        }
        onMouseClicked = {
            if (!this.isDisabled) {
                addButton4.visual = ImageVisual("images/AddPlayer.png")
                addButton4.isDisabled = false
                addAI3.isDisabled = false
                addAI3.visual = ImageVisual("images/AddAIButton.png")
                p3Input.isDisabled = false
                p3Input.text = namesList.random().also{namesList.remove(it)}
                removeButton3.isDisabled = false
                removeButton3.visual = ImageVisual("images/RemovePlayer.png")
                this.visual = Visual.EMPTY
                this.isDisabled = true
                countPlayers()
            }
        }
    }

    /**
     * Adds [removeButton4], activates [p4Input], and removes itself with [removeButton3].
     */
    private val addButton4 = Button(
        width = addButton3.width, height = addButton3.height, posX = addButton3.posX, posY = p4Input.posY,
    ).apply {
        this.isDisabled = true
        onMouseEntered = {
            if (!this.isDisabled) this.visual = ImageVisual("images/AddPlayerHover.png")
        }
        onMouseExited = {
            if (!this.isDisabled) this.visual = ImageVisual("images/AddPlayer.png")
        }
        onMouseClicked = {
            if (!this.isDisabled) {
                addButton5.visual = ImageVisual("images/AddPlayer.png")
                addButton5.isDisabled = false
                addAI4.isDisabled = false
                addAI4.visual = ImageVisual("images/AddAIButton.png")
                p4Input.isDisabled = false
                p4Input.text = namesList.random().also{namesList.remove(it)}
                removeButton3.isDisabled = true
                removeButton3.visual = Visual.EMPTY
                removeButton4.isDisabled = false
                removeButton4.visual = ImageVisual("images/RemovePlayer.png")
                this.visual = Visual.EMPTY
                this.isDisabled = true
                countPlayers()
            }
        }
    }

    private val addButton5 = Button(
        width = addButton3.width, height = addButton3.height, posX = addButton3.posX, posY = p5Input.posY,
    ).apply {
        this.isDisabled = true
        onMouseEntered = {
            if (!this.isDisabled) this.visual = ImageVisual("images/AddPlayerHover.png")
        }
        onMouseExited = {
            if (!this.isDisabled) this.visual = ImageVisual("images/AddPlayer.png")
        }
        onMouseClicked = {
            if (!this.isDisabled) {
                addAI5.isDisabled = false
                addAI5.visual = ImageVisual("images/AddAIButton.png")
                p5Input.isDisabled = false
                p5Input.text = namesList.random().also{namesList.remove(it)}
                removeButton4.isDisabled = true
                removeButton4.visual = Visual.EMPTY
                removeButton5.isDisabled = false
                removeButton5.visual = ImageVisual("images/RemovePlayer.png")
                this.visual = Visual.EMPTY
                this.isDisabled = true
                countPlayers()
            }
        }
    }

    /**
     * Removes itself, deactivates [p3Input] and adds [addButton3].
     */
    private val removeButton3: Button = Button(
        width = addButton3.width, height = addButton3.height, posX = addButton3.posX, posY = addButton3.posY
    ).apply {
        this.isDisabled = true
        onMouseEntered = {
            if (!this.isDisabled) this.visual = ImageVisual("images/RemovePlayerHover.png")
        }
        onMouseExited = {
            if (!this.isDisabled) this.visual = ImageVisual("images/RemovePlayer.png")
        }
        onMouseClicked = {
            if (!this.isDisabled) {
                namesList.add(p3Input.text)
                this.visual = Visual.EMPTY
                p3Input.text = "Add player 3"
                p3Input.isDisabled = true
                addButton3.isDisabled = false
                addButton3.visual = ImageVisual("images/AddPlayer.png")
                addButton4.visual = Visual.EMPTY
                addButton4.isDisabled = true
                this.isDisabled = true
                /* Enabled means AI is set and the dropdown has to be removed */
                aI3Difficulty.isDisabled = true
                if(!addAI3.isDisabled) {
                    removeComponents(aI3Difficulty)
                }
                addAI3.isDisabled = true
                addAI3.visual = ImageVisual("images/AddAIButtonDisabled.png")
                countPlayers()
            }
        }
    }

    /**
     * Removes  itself, deactivates [p4Input] and adds [removeButton3], [addButton3].
     */
    private val removeButton4: Button = Button(
        width = addButton4.width, height = addButton4.height, posX = addButton4.posX, posY = addButton4.posY,
        visual = Visual.EMPTY
    ).apply {
        this.isDisabled = true
        onMouseEntered = {
            if (!this.isDisabled) this.visual = ImageVisual("images/RemovePlayerHover.png")
        }
        onMouseExited = {
            if (!this.isDisabled) this.visual = ImageVisual("images/RemovePlayer.png")
        }
        onMouseClicked = {
            if (!this.isDisabled) {
                namesList.add(p4Input.text)
                this.visual = Visual.EMPTY
                p4Input.text = "Add player 4"
                p4Input.isDisabled = true
                addButton4.isDisabled = false
                addButton4.visual = ImageVisual("images/AddPlayer.png")
                addButton5.visual = Visual.EMPTY
                addButton5.isDisabled = true
                removeButton3.isDisabled = false
                removeButton3.visual = ImageVisual("images/RemovePlayer.png")
                this.isDisabled = true
                /* Enabled means AI is set and the dropdown has to be removed */
                aI4Difficulty.isDisabled = true
                if(!addAI4.isDisabled) {
                    removeComponents(aI4Difficulty)
                }
                addAI4.isDisabled = true
                addAI4.visual = ImageVisual("images/AddAIButtonDisabled.png")
                countPlayers()
            }
        }
    }

    /**
     * Removes  itself, deactivates [p4Input] and adds [removeButton3], [addButton3].
     */
    private val removeButton5: Button = Button(
        width = addButton5.width, height = addButton5.height, posX = addButton5.posX, posY = addButton5.posY,
        visual = Visual.EMPTY
    ).apply {
        this.isDisabled = true
        onMouseEntered = {
            if (!this.isDisabled) this.visual = ImageVisual("images/RemovePlayerHover.png")
        }
        onMouseExited = {
            if (!this.isDisabled) this.visual = ImageVisual("images/RemovePlayer.png")
        }
        onMouseClicked = {
            if (!this.isDisabled) {
                namesList.add(p5Input.text)
                this.visual = Visual.EMPTY
                p5Input.text = "Add player 5"
                p5Input.isDisabled = true
                addButton5.isDisabled = false
                addButton5.visual = ImageVisual("images/AddPlayer.png")
                removeButton4.isDisabled = false
                removeButton4.visual = ImageVisual("images/RemovePlayer.png")
                this.isDisabled = true
                /* Enabled means AI is set and the dropdown has to be removed */
                aI5Difficulty.isDisabled = true
                if(!addAI5.isDisabled) {
                    removeComponents(aI5Difficulty)
                }
                addAI5.isDisabled = true
                addAI5.visual = ImageVisual("images/AddAIButtonDisabled.png")
                countPlayers()
            }
        }
    }

    /**
     * Shuffle button / toggle.
     */
    private val shuffleToggle = Label(
        width = 128, height = 128, posX = p1Input.posX + 150, posY = p5Input.posY + 150,
        visual = ImageVisual("images/ShuffleDisabled.png")
    ).apply {
        isDisabled = false
        onMouseEntered = {
            visual = if (shufflePlayers) {
                ImageVisual("images/ShuffleHover.png")
            } else {
                ImageVisual("images/ShuffleDisabledHover.png")
            }
            tooltipViewer.visual = improvedUI.showTooltipVisual(
                component = this,
                message = "Play in a shuffled order?"
            )
        }
        onMouseExited = {
            visual = if (shufflePlayers) {
                ImageVisual("images/Shuffle.png")
            } else {
                ImageVisual("images/ShuffleDisabled.png")
            }
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            visual = if (!shufflePlayers) {
                ImageVisual("images/Shuffle.png")
            } else {
                ImageVisual("images/ShuffleDisabled.png")
            }
            shufflePlayers = !shufflePlayers
        }
    }

    /**
     * Starts the game. Enabled if at least 2/4 players at entered.
     * This guarantee is provided by the function [countPlayers].
     */
    private val startButton = Button(
        width = 200, height = 172, posX = 1550, posY = 842,
        alignment = Alignment.CENTER,
        visual = ImageVisual("images/Pikachu_Start_Button_White.png")
    ).apply {
        if (!this.isDisabled) {
            onMouseEntered = {
                if (countPlayers() >= 2) {
                    this.visual = ImageVisual("images/Pikachu_Start_Button_White.png")
                }
            }
            onMouseExited = {
                if (countPlayers() >= 2) {
                    this.visual = ImageVisual("images/Pikachu_Start_Button_White.png")
                }
            }
            onMouseClicked = {
                val players = mutableListOf<Player>()
                for (field in listOf(p1Input, p2Input, p3Input, p4Input, p5Input)) {
                    if (!field.isDisabled && field.text.isNotBlank()) {
                        players.add(Player.HumanPlayer(field.text.trim()))
                    }
                    if (field.isDisabled && !playerFields.forward(field).isDisabled &&
                        playerFields.forward(field).selectedItem != null) {
                        players.add(Player.AIPlayer(
                            name = playerFields.forward(field).selectedItem!!.substringBefore(':')
                                    +" ["+playerFields.forward(field).selectedItem!!.substringAfter(':').trim().first()+"]",
                            difficulty = AIDifficulty.valueOf(
                                playerFields.forward(field).selectedItem!!.substringAfter(':').trim().uppercase()
                            )
                        ))
                    }
                }
                if(shufflePlayers) {
                    players.shuffle()
                }
                val sameName = players.groupBy { it.getPlayerName() }.any{it.value.size>1}
                /* In case option is checked, cards will be loaded externally */
                if(loadFileCheck.isChecked && cardsFile != null) {
                    /* Also make sure that the preset suits player count */
                    if(countPlayers() == cardsFile!!.readText().first().digitToInt()) {
                        //println("${cardsFile.first().digitToInt()}")
                        if(!sameName) {
                            rootService.gameService.startNewGame(players, cardsFile)
                        } else {
                            app.showDialog(
                                Dialog(
                                    dialogType = DialogType.ERROR,
                                    title = "Start game failed :(",
                                    header = "Players names are not unique!",
                                    message = "Please make sure that each player's name is unique."
                                )
                            )
                        }
                    } else {
                        app.showDialog(
                            Dialog(
                                dialogType = DialogType.ERROR,
                                title = "Start game failed :(",
                                header = "Player count not suitable for given preset.",
                                message = " ${cardsFile!!.readText().first()} players are required for the given preset."
                            )
                        )
                    }
                } else {
                    if(!sameName) {
                        rootService.gameService.startNewGame(players)
                    } else {
                        app.showDialog(
                            Dialog(
                                dialogType = DialogType.ERROR,
                                title = "Start game failed :(",
                                header = "Players names are not unique!",
                                message = "Please make sure that each player's name is unique."
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * GG
     */
    val exitButton = Button(
        width = 200, height = 172, posX = 170, posY = 860,
        alignment = Alignment.CENTER,
        visual = ImageVisual("images/Snorlax_Quit_Button_New.png"),

        ).apply {
        if (!this.isDisabled) {
            onMouseEntered = {
                this.visual = ImageVisual("images/Snorlax_Quit_Button_New.png")

            }
            onMouseExited = {
                this.visual = ImageVisual("images/Snorlax_Quit_Button_New.png")
            }
        }
    }

    /**
     * File loader used for loading a preset stack of cards.
     */
    private val loadFile: Button = Button(
        posX = 1600, posY = 620, width = 90, height = 90,
        visual = ImageVisual("images/LoadFile.png")
    ).apply {
        onMouseEntered = {
            visual = ImageVisual("images/LoadFileHover.png")
            tooltipViewer.visual = improvedUI.showTooltipVisual(
                component = this,
                message = "Load cards as preset"
            )
        }
        onMouseExited = {
            visual = ImageVisual("images/LoadFile.png")
            improvedUI.hideTooltip()
        }
        onMouseClicked = {
            if(!isDisabled) {
                app.showFileDialog(
                    FileDialog(
                        mode = FileDialogMode.OPEN_FILE,
                        title = "Load stack",
                    )
                ).ifPresent { files ->
                    println("Loaded ${files[0]}")
                    /* Only accept txt files, otherwise throw an error message */
                    if(files[0].extension == "txt") {
                        cardsFile = files[0]
                        loadedCardsFileName.text = files[0].name
                        loadFileCheck.isChecked = true
                        println(cardsFile)
                    } else {
                        app.showDialog(
                            Dialog(
                                dialogType = DialogType.ERROR,
                                title = "Load cards failed :(",
                                header = "Invalid file extension!",
                                message = "Please select a .txt file! It's not that hard."
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Checks whether game is to be initialized with a given set of cards, or at random
     */
    private val loadFileCheck: CheckBox = CheckBox(
        posX = loadFile.posX - 30, posY = loadFile.posY + 120, width = 32, height = 32,
        text = "Use preset", font = Font(size = 24)
    ).apply {
        isCheckedProperty.addListener { _,_ ->
            //loadFile.isDisabled = !loadFile.isDisabled
        }
    }

    /**
     * Reads the loaded file name of the given cards preset.
     */
    private val loadedCardsFileName: Label = Label(
        posX = loadFileCheck.posX - 20, posY = loadFileCheck.posY + 40, height = 40, width = 200,
        text = "", font = Font(size = 20)
    )

    init {
        background = CompoundVisual(
            // ImageVisual("images/Background.jpg"),
            ColorVisual(213, 232, 212, 255)
        )
        opacity = 1.0
        addComponents(
            backgroundLower, tuButton, tooltipViewer,
            p1Input, addAI1,
            p2Input, addAI2,
            addButton3, p3Input, removeButton3, addAI3,
            addButton4, p4Input, removeButton4, addAI4,
            addButton5, p5Input, removeButton5, addAI5,
            shuffleToggle, startButton, exitButton,
            loadFile, loadFileCheck, loadedCardsFileName
        )
        improvedUI.tooltipViewer = tooltipViewer
        /* For unknown reason, probably image file formats, white boxes are being displayed at the start */
        addButton4.visual = Visual.EMPTY
        addButton5.visual = Visual.EMPTY
        removeButton3.visual = Visual.EMPTY
        playerFields.addAll(
            Pair(p1Input, aI1Difficulty),
            Pair(p2Input, aI2Difficulty),
            Pair(p3Input, aI3Difficulty),
            Pair(p4Input, aI4Difficulty),
            Pair(p5Input, aI5Difficulty),
        )
        countPlayers()
    }

    /**
     * This function serves as a security layer to prevent starting the game with
     * less than 2 players. It is called upon any change in the input value in each of
     * [p1Input], [p2Input], [p3Input] and [p4Input] as well as upon clicking any of
     * [addButton3], [addButton4], [removeButton3] and [removeButton4]. It can both
     * enable and disable the "Start" button depending on how many valid player names
     * in total are entered.
     * @return the number of entered player names (spaces don't count).
     */
    private fun countPlayers(): Int {
        var n = 0
        for (field in listOf(p1Input, p2Input, p3Input, p4Input, p5Input)) {
            if (!field.isDisabled && field.text.isNotBlank()) {
                n++
            }
        }
        for (aiField in listOf(aI1Difficulty, aI2Difficulty, aI3Difficulty, aI4Difficulty, aI5Difficulty)) {
            if (!aiField.isDisabled && aiField.selectedItem != null) {
                n++
            }
        }
        startButton.isDisabled = n < 2
        startButton.visual =
            if (n < 2) ImageVisual("images/Pikachu_Start_Button_White.png")
            else ImageVisual("images/Pikachu_Start_Button_White.png")
        return n
    }
}