package com.lab1.mygame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Date
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var gameBoard: GridLayout
    private lateinit var gameStatus: TextView
    private lateinit var gameHistoryList: ListView
    private lateinit var maxWinStreak: TextView

    private val buttons = Array(3) { arrayOfNulls<Button>(3) }
    private var board = Array(3) { IntArray(3) }
    private var isPlayerTurn = true
    private var gameHistory = mutableListOf<GameResult>()
    private var currentWinStreak = 0
    private var maxWinStreakCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameBoard = findViewById(R.id.gameBoard)
        gameStatus = findViewById(R.id.tvGameStatus)
        gameHistoryList = findViewById(R.id.lvGameHistory)
        maxWinStreak = findViewById(R.id.tvMaxWinStreak)

        findViewById<Button>(R.id.btnNewGame).setOnClickListener { startNewGame() }

        updateGameHistoryUI()
    }

    private fun startNewGame() {
        // Очищаем доску
        board = Array(3) { IntArray(3) }
        gameBoard.removeAllViews()

        // Создаем кнопки для игрового поля
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                val button = Button(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(j, 1f) // Добавляем weight здесь
                        rowSpec = GridLayout.spec(i, 1f)    // Добавляем weight здесь
                        setMargins(4, 4, 4, 4)
                    }
                    textSize = 32f
                    setOnClickListener { onCellClick(i, j) }
                }
                buttons[i][j] = button
                gameBoard.addView(button)
            }
        }

        isPlayerTurn = true
        gameStatus.text = getString(R.string.player_turn)
    }

    private fun onCellClick(row: Int, col: Int) {
        if (board[row][col] != 0 || !isPlayerTurn) return

        // Ход игрока
        board[row][col] = 1
        buttons[row][col]?.text = "X"

        if (checkWinner(1)) {
            endGame(true)
            return
        } else if (isBoardFull()) {
            endGame(null)
            return
        }

        isPlayerTurn = false
        gameStatus.text = getString(R.string.computer_turn)

        // Ход компьютера с небольшой задержкой
        gameBoard.postDelayed({ computerMove() }, 500)
    }

    private fun computerMove() {
        if (isPlayerTurn) return

        // Простой ИИ: сначала проверяем возможность выиграть, затем блокируем игрока, затем случайный ход
        val (row, col) = findBestMove()
        board[row][col] = 2
        buttons[row][col]?.text = "O"

        if (checkWinner(2)) {
            endGame(false)
            return
        } else if (isBoardFull()) {
            endGame(null)
            return
        }

        isPlayerTurn = true
        gameStatus.text = getString(R.string.player_turn)
    }

    private fun findBestMove(): Pair<Int, Int> {
        // 1. Проверяем, может ли компьютер выиграть
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == 0) {
                    board[i][j] = 2
                    if (checkWinner(2)) {
                        board[i][j] = 0
                        return Pair(i, j)
                    }
                    board[i][j] = 0
                }
            }
        }

        // 2. Блокируем игрока
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == 0) {
                    board[i][j] = 1
                    if (checkWinner(1)) {
                        board[i][j] = 0
                        return Pair(i, j)
                    }
                    board[i][j] = 0
                }
            }
        }

        // 3. Случайный ход
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }

        return if (emptyCells.isNotEmpty()) {
            emptyCells[Random.nextInt(emptyCells.size)]
        } else {
            Pair(0, 0)
        }
    }

    private fun checkWinner(player: Int): Boolean {
        // Проверка строк и столбцов
        for (i in 0 until 3) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true
        }

        // Проверка диагоналей
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true

        return false
    }

    private fun isBoardFull(): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == 0) return false
            }
        }
        return true
    }

    private fun endGame(isPlayerWin: Boolean?) {
        val result = when (isPlayerWin) {
            true -> {
                currentWinStreak++
                if (currentWinStreak > maxWinStreakCount) {
                    maxWinStreakCount = currentWinStreak
                }
                GameResult(Date(), getString(R.string.player_wins), true)
            }
            false -> {
                currentWinStreak = 0
                GameResult(Date(), getString(R.string.computer_wins), false)
            }
            null -> {
                currentWinStreak = 0
                GameResult(Date(), getString(R.string.game_draw), false)
            }
        }

        gameHistory.add(result)
        updateGameHistoryUI()

        gameStatus.text = when (isPlayerWin) {
            true -> getString(R.string.player_wins)
            false -> getString(R.string.computer_wins)
            null -> getString(R.string.game_draw)
        }
    }

    private fun updateGameHistoryUI() {
        maxWinStreak.text = getString(R.string.max_streak, maxWinStreakCount)
        gameHistoryList.adapter = GameHistoryAdapter(
            this,
            android.R.layout.simple_list_item_1,
            gameHistory.reversed()
        )
    }
}
