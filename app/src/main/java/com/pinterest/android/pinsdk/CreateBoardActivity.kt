package com.pinterest.android.pinsdk

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.pinterest.android.pdk.PDKCallback
import com.pinterest.android.pdk.PDKClient
import com.pinterest.android.pdk.PDKException
import com.pinterest.android.pdk.PDKResponse
import com.pinterest.android.pdk.Utils

class CreateBoardActivity : ActionBarActivity() {

    lateinit var boardName: EditText
    lateinit var boardDesc: EditText
    lateinit var saveButton: Button
    lateinit var responseView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        title = "New Board"

        boardName = findViewById(R.id.board_create_name) as EditText
        boardDesc = findViewById(R.id.board_create_desc) as EditText
        responseView = findViewById(R.id.board_response_view) as TextView
        saveButton = findViewById(R.id.save_button) as Button

        saveButton.setOnClickListener { onSaveBoard() }

    }

    private fun onSaveBoard() {
        val bName = boardName.text.toString()
        if (!Utils.isEmpty(bName)) {
            PDKClient.instance.createBoard(bName, boardDesc.text.toString(), object : PDKCallback() {
                override fun onSuccess(response: PDKResponse) {
                    Log.d(javaClass.name, response.data!!.toString())
                    responseView.text = response.data!!.toString()

                }

                override fun onFailure(exception: PDKException) {
                    Log.e(javaClass.name, exception.detailMessage)
                    responseView.text = exception.detailMessage
                }
            })
        } else {
            Toast.makeText(this, "Board name cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }
}
