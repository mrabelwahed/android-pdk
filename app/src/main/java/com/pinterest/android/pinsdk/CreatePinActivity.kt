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


class CreatePinActivity : ActionBarActivity() {

    lateinit var imageUrl: EditText
    lateinit var link: EditText
    lateinit var boardId: EditText
    lateinit var note: EditText
    lateinit var saveButton: Button
    internal var selectImagebutton: Button? = null
    lateinit var responseView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pin)
        title = "New Pin"
        imageUrl = findViewById(R.id.pin_create_url) as EditText
        link = findViewById(R.id.pin_create_link) as EditText
        note = findViewById(R.id.pin_create_note) as EditText
        responseView = findViewById(R.id.pin_response_view) as TextView
        boardId = findViewById(R.id.create_pin_board_id) as EditText
        saveButton = findViewById(R.id.save_button) as Button
        saveButton.setOnClickListener { onSavePin() }
    }

    private fun onSavePin() {
        val pinImageUrl = imageUrl.text.toString()
        val board = boardId.text.toString()
        val noteText = note.text.toString()
        if (!Utils.isEmpty(noteText) && !Utils.isEmpty(board) && !Utils.isEmpty(pinImageUrl)) {
            PDKClient
                    .instance.createPin(noteText, board, pinImageUrl, link.text.toString(), object : PDKCallback() {
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
            Toast.makeText(this, "Required fields cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }
}
