package hk.edu.thei.ict.gameproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {

    // This function runs when the login activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the login screen layout from activity_main.xml.
        setContentView(R.layout.activity_main)

        // Connect the Kotlin variables to the UI components.
        val usernameInput = findViewById<EditText>(R.id.edtUsername)
        val passwordInput = findViewById<EditText>(R.id.edtPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        // Listen for the user's login button click.
        loginButton.setOnClickListener {

            // Read the username and remove unnecessary spaces.
            val username = usernameInput.text.toString().trim()

            // Read the password entered by the user.
            val password = passwordInput.text.toString()

            // Check whether the username or password is empty.
            if (username.isEmpty() || password.isEmpty()) {

                // Display an error message if either field is empty.
                Toast.makeText(
                    this,
                    getString(R.string.login_empty_error),
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Open the application's SharedPreferences storage.
                val prefs = getSharedPreferences(
                    "GameSettings",
                    MODE_PRIVATE
                )

                // Save the player's username for use in the game.
                prefs.edit()
                    .putString("player_name", username)
                    .apply()

                // Inform the player that login was successful.
                Toast.makeText(
                    this,
                    getString(R.string.login_success),
                    Toast.LENGTH_SHORT
                ).show()

                // Prepare to open the Bingo game screen.
                val intent = Intent(
                    this@MainActivity,
                    GameActivity::class.java
                )

                // Navigate from the login screen to the game screen.
                startActivity(intent)
            }
        }
    }
}
