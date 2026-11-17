package hk.edu.thei.ict.gameproject

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
    }

    private lateinit var playerNameInput: EditText
    private lateinit var markGroup: RadioGroup
    private lateinit var themeGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSettingsUI()
    }

    private fun createSettingsUI() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = getString(R.string.settings_title)
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val nameLabel = TextView(this).apply {
            text = getString(R.string.player_name_label)
            textSize = 18f
        }

        playerNameInput = EditText(this).apply {
            hint = getString(R.string.player_name_hint)
            setSingleLine(true)
            setText(prefs.getString("player_name", "Player"))
        }

        val markLabel = TextView(this).apply {
            text = getString(R.string.player_mark_label)
            textSize = 18f
        }

        markGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }

        val xRadio = RadioButton(this).apply {
            id = MARK_X_ID
            text = "X"
        }

        val oRadio = RadioButton(this).apply {
            id = MARK_O_ID
            text = "O"
        }

        markGroup.addView(xRadio)
        markGroup.addView(oRadio)

        val savedMark = prefs.getString("player_mark", "X")
        markGroup.check(
            if (savedMark == "O") MARK_O_ID else MARK_X_ID
        )

        val themeLabel = TextView(this).apply {
            text = getString(R.string.board_theme_label)
            textSize = 18f
        }

        themeGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        val themes = listOf(
            "Classic" to R.string.theme_classic,
            "Green" to R.string.theme_green,
            "Blue" to R.string.theme_blue
        )

        themes.forEach { (theme, labelId) ->

            val radio = RadioButton(this).apply {
                id = when (theme) {
                    "Green" -> THEME_GREEN_ID
                    "Blue" -> THEME_BLUE_ID
                    else -> THEME_CLASSIC_ID
                }

                text = getString(labelId)
            }

            themeGroup.addView(radio)
        }

        val savedTheme = prefs.getString("board_theme", "Classic")

        themeGroup.check(
            when (savedTheme) {
                "Green" -> THEME_GREEN_ID
                "Blue" -> THEME_BLUE_ID
                else -> THEME_CLASSIC_ID
            }
        )

        val saveButton = Button(this).apply {
            text = getString(R.string.save_settings)

            setOnClickListener {
                saveSettings()
            }
        }

        val returnButton = Button(this).apply {
            text = getString(R.string.return_to_game)

            setOnClickListener {
                finish()
            }
        }

        layout.addView(title)
        layout.addView(nameLabel)
        layout.addView(playerNameInput)
        layout.addView(markLabel)
        layout.addView(markGroup)
        layout.addView(themeLabel)
        layout.addView(themeGroup)
        layout.addView(saveButton)
        layout.addView(returnButton)

        setContentView(layout)
    }

    private fun saveSettings() {

        val playerName = playerNameInput.text.toString()
            .trim()
            .ifEmpty { "Player" }

        val playerMark =
            if (markGroup.checkedRadioButtonId == MARK_O_ID) {
                "O"
            } else {
                "X"
            }

        val boardTheme = when (themeGroup.checkedRadioButtonId) {
            THEME_GREEN_ID -> "Green"
            THEME_BLUE_ID -> "Blue"
            else -> "Classic"
        }

        prefs.edit()
            .putString("player_name", playerName)
            .putString("player_mark", playerMark)
            .putString("board_theme", boardTheme)
            .putBoolean("settings_changed", true)
            .apply()

        Toast.makeText(
            this,
            getString(R.string.settings_saved),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    companion object {
        private const val MARK_X_ID = 1001
        private const val MARK_O_ID = 1002

        private const val THEME_CLASSIC_ID = 2001
        private const val THEME_GREEN_ID = 2002
        private const val THEME_BLUE_ID = 2003
    }
}
