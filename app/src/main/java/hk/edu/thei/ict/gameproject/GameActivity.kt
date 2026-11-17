package hk.edu.thei.ict.gameproject

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class GameActivity : Activity() {

    // Store the player's name and current round number.
    private var playerName = "Player"
    private var roundNumber = 1

    // Store the five randomly generated Bingo numbers.
    private var bingoNumbers = mutableListOf<Int>()

    // Store all numbers that have already been drawn.
    private val drawnNumbers = mutableSetOf<Int>()

    // Store references to the screen components.
    private lateinit var titleText: TextView
    private lateinit var roundText: TextView
    private lateinit var statusText: TextView
    private lateinit var numbersText: TextView
    private lateinit var drawnText: TextView
    private lateinit var drawButton: Button
    private lateinit var newRoundButton: Button

    // Track whether the current round has finished.
    private var gameOver = false

    // Track whether the current game record has been saved.
    private var recordSaved = false

    // Create the SQLite database helper.
    private lateinit var databaseHelper: DatabaseHelper

    // Access SharedPreferences to retrieve player information.
    private val prefs by lazy {
        getSharedPreferences("GameSettings", MODE_PRIVATE)
    }

    // Create the Bingo game when the activity starts.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the player's saved username.
        playerName = prefs.getString("player_name", "Player") ?: "Player"

        // Retrieve the saved round number.
        roundNumber = prefs.getInt("bingo_round", 0)

        // Start the first round if no previous round exists.
        if (roundNumber == 0) {
            roundNumber = 1
            prefs.edit().putInt("bingo_round", roundNumber).apply()
        }

        // Initialize the SQLite database helper.
        databaseHelper = DatabaseHelper(this)

        // Generate the first set of Bingo numbers.
        generateBingoNumbers()

        // Create and display the game interface.
        createGameUI()
    }

    // Generate five unique random numbers between 1 and 75.
    private fun generateBingoNumbers() {
        bingoNumbers = (1..75).shuffled().take(5).sorted().toMutableList()

        // Clear the numbers drawn in the previous round.
        drawnNumbers.clear()

        // Allow the player to start a new round.
        gameOver = false

        // Reset the database saving status for the new round.
        recordSaved = false
    }

    // Build the Bingo game interface programmatically.
    private fun createGameUI() {

        // Create the main vertical layout.
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            setBackgroundColor(Color.rgb(245, 247, 250))

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Display the game title.
        titleText = TextView(this).apply {
            text = "BINGO GAME"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(35, 55, 85))
        }

        // Display the player's name.
        val welcomeText = TextView(this).apply {
            text = "Welcome, $playerName!"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, dpToPx(12), 0, dpToPx(12))
        }

        // Display the current round number.
        roundText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }

        // Display instructions and game results.
        statusText = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(35, 90, 160))
            setPadding(0, dpToPx(20), 0, dpToPx(20))
        }

        // Display the five Bingo numbers.
        numbersText = TextView(this).apply {
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(30, 70, 130))
            setPadding(dpToPx(8), dpToPx(20), dpToPx(8), dpToPx(20))
        }

        // Display all numbers drawn by the system.
        drawnText = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, dpToPx(12), 0, dpToPx(20))
        }

        // Create the button used to draw the next number.
        drawButton = Button(this).apply {
            text = "Draw Number"
            textSize = 16f
            setOnClickListener {
                drawNumber()
            }
        }

        // Create the button used to start another round.
        newRoundButton = Button(this).apply {
            text = "New Round"
            textSize = 16f
            setOnClickListener {
                startNewRound()
            }
        }

        // Create a button for accessing the configuration screen.
        val settingsButton = Button(this).apply {
            text = "Settings"
            setOnClickListener {
                val intent = Intent(
                    this@GameActivity,
                    SettingsActivity::class.java
                )
                startActivity(intent)
            }
        }

        // Add all interface components to the main layout.
        rootLayout.addView(titleText)
        rootLayout.addView(welcomeText)
        rootLayout.addView(roundText)
        rootLayout.addView(statusText)
        rootLayout.addView(numbersText)
        rootLayout.addView(drawnText)
        rootLayout.addView(drawButton)
        rootLayout.addView(newRoundButton)
        rootLayout.addView(settingsButton)

        // Display the completed interface.
        setContentView(rootLayout)

        // Display the initial game information.
        updateGameUI()
    }

    // Draw one random number that has not been drawn before.
    private fun drawNumber() {

        // Stop the function if the current round has finished.
        if (gameOver) {
            return
        }

        // Find all numbers that have not been drawn.
        val availableNumbers = (1..75).filter {
            it !in drawnNumbers
        }

        // Stop drawing if no numbers are available.
        if (availableNumbers.isEmpty()) {
            statusText.text = "No more numbers available!"
            return
        }

        // Randomly select one number from the remaining numbers.
        val selectedNumber = availableNumbers.random()

        // Add the selected number to the drawn number collection.
        drawnNumbers.add(selectedNumber)

        // Check whether all five Bingo numbers have been matched.
        checkBingo()

        // Refresh the information displayed on the screen.
        updateGameUI()
    }

    // Check whether all five Bingo numbers have been drawn.
    private fun checkBingo() {

        // Count how many Bingo numbers have been matched.
        val matchedCount = bingoNumbers.count {
            it in drawnNumbers
        }

        // The player wins when all five numbers are matched.
        if (matchedCount == 5) {

            // Mark the current round as completed.
            gameOver = true

            // Display the winning message.
            statusText.text = "BINGO! YOU WIN!"

            // Disable the draw button after winning.
            drawButton.isEnabled = false

            // Save the completed game record into SQLite.
            if (!recordSaved) {
                val saved = databaseHelper.saveGameRecord(
                    playerName,
                    roundNumber,
                    bingoNumbers
                )

                // Display the result of the database operation.
                if (saved) {
                    recordSaved = true
                    Toast.makeText(
                        this,
                        "Game record saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to save game record.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else {

            // Display the current number of matched numbers.
            statusText.text = "Matched: $matchedCount / 5"
        }
    }

    // Start a new Bingo round.
    private fun startNewRound() {

        // Increase the round number.
        roundNumber++

        // Save the latest round number using SharedPreferences.
        prefs.edit()
            .putInt("bingo_round", roundNumber)
            .apply()

        // Generate a new set of five Bingo numbers.
        generateBingoNumbers()

        // Enable the draw button for the new round.
        drawButton.isEnabled = true

        // Refresh the game interface.
        updateGameUI()
    }

    // Refresh all game information displayed on the screen.
    private fun updateGameUI() {

        // Display the current round number.
        roundText.text = "Round: $roundNumber"

        // Display the five Bingo numbers.
        numbersText.text = bingoNumbers.joinToString("     ")

        // Display the numbers that have already been drawn.
        drawnText.text = if (drawnNumbers.isEmpty()) {
            "Drawn Numbers: None"
        } else {
            "Drawn Numbers: ${drawnNumbers.joinToString(", ")}"
        }

        // Update the game status before the player starts drawing.
        if (!gameOver && drawnNumbers.isEmpty()) {
            statusText.text = "Press Draw Number to begin!"
        }

        // Enable or disable the draw button based on the game status.
        drawButton.isEnabled = !gameOver
    }

    // Close the database connection when the activity is destroyed.
    override fun onDestroy() {
        if (::databaseHelper.isInitialized) {
            databaseHelper.close()
        }
        super.onDestroy()
    }

    // Convert density-independent pixels into screen pixels.
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
