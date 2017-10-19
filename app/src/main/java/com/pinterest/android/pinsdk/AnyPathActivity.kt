package com.pinterest.android.pinsdk

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView

import com.pinterest.android.pdk.PDKCallback
import com.pinterest.android.pdk.PDKClient
import com.pinterest.android.pdk.PDKException
import com.pinterest.android.pdk.PDKResponse
import com.pinterest.android.pdk.Utils


import java.util.HashMap


class AnyPathActivity : ActionBarActivity(), AdapterView.OnItemSelectedListener {

    private var pathText: EditText? = null
    private var fieldsText: EditText? = null
    private var spinner: Spinner? = null
    private var getButton: Button? = null
    private var responseView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_any_path)
        title = "Get Path.."

        pathText = findViewById(R.id.path_edittext) as EditText
        fieldsText = findViewById(R.id.fields_edittext) as EditText
        responseView = findViewById(R.id.path_response_view) as TextView
        responseView!!.movementMethod = ScrollingMovementMethod()
        getButton = findViewById(R.id.get_button) as Button
        getButton!!.setOnClickListener { onGet() }

        spinner = findViewById(R.id.spinner) as Spinner
        spinner!!.onItemSelectedListener = this
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.paths_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.adapter = adapter
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                pos: Int, id: Long) {
        when (pos) {
            0 -> pathText!!.setText("pins/158400111869919209/")
            1 -> pathText!!.setText("boards/158400180583006164/")
            2 -> pathText!!.setText("me/likes/")
            3 -> pathText!!.setText("me/following/interests/")
            4 -> pathText!!.setText("boards/158400180583006164/pins/")
            5 -> pathText!!.setText("users/8en/")
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun onGet() {
        val path = pathText!!.text.toString()
        val fields = fieldsText!!.text.toString()
        val params = HashMap<String, String>()
        params.put(PDKClient.PDK_QUERY_PARAM_FIELDS, fields)
        if (!Utils.isEmpty(path)) {
            PDKClient
                    .instance.getPath(path, params, object : PDKCallback() {
                override fun onSuccess(response: PDKResponse) {
                    Log.d(javaClass.name, response.data!!.toString())
                    responseView!!.text = response.data!!.toString()
                }

                override fun onFailure(exception: PDKException) {
                    Log.e(javaClass.name, exception.detailMessage)
                    responseView!!.text = exception.detailMessage
                }
            })
        }
    }
}
