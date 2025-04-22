package com.lab1.mygame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class GameHistoryAdapter(context: Context, private val resource: Int, private val gameResults: List<GameResult>) :
    ArrayAdapter<GameResult>(context, resource, gameResults) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val gameResult = gameResults[position]

        view.findViewById<TextView>(android.R.id.text1).text =
            "${dateFormat.format(gameResult.date)} - ${gameResult.winner}"

        return view
    }
}
