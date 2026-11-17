package hk.edu.thei.ict.gameproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BingoGame.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "game_records"

        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_ROUND = "round_number"
        const val COLUMN_NUMBER1 = "bingo_number1"
        const val COLUMN_NUMBER2 = "bingo_number2"
        const val COLUMN_NUMBER3 = "bingo_number3"
        const val COLUMN_NUMBER4 = "bingo_number4"
        const val COLUMN_NUMBER5 = "bingo_number5"
        const val COLUMN_TIME = "system_time"
    }

    // Create the database table when the database is first opened.
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_ROUND INTEGER,
                $COLUMN_NUMBER1 INTEGER,
                $COLUMN_NUMBER2 INTEGER,
                $COLUMN_NUMBER3 INTEGER,
                $COLUMN_NUMBER4 INTEGER,
                $COLUMN_NUMBER5 INTEGER,
                $COLUMN_TIME TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    // Update the database structure when its version changes.
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Save one completed Bingo game into the database.
    fun saveGameRecord(
        username: String,
        roundNumber: Int,
        bingoNumbers: List<Int>
    ): Boolean {

        // Make sure that exactly five Bingo numbers are provided.
        if (bingoNumbers.size != 5) {
            return false
        }

        // Get the current system date and time.
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )
        val currentTime = dateFormat.format(Date())

        // Prepare all values before inserting the record.
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_ROUND, roundNumber)
            put(COLUMN_NUMBER1, bingoNumbers[0])
            put(COLUMN_NUMBER2, bingoNumbers[1])
            put(COLUMN_NUMBER3, bingoNumbers[2])
            put(COLUMN_NUMBER4, bingoNumbers[3])
            put(COLUMN_NUMBER5, bingoNumbers[4])
            put(COLUMN_TIME, currentTime)
        }

        // Insert the completed game record into SQLite.
        val database = writableDatabase
        val result = database.insert(TABLE_NAME, null, values)

        // Return true if the record was successfully inserted.
        return result != -1L
    }
}
