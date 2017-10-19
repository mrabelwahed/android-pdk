package com.pinterest.android.pinsdk

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.pinterest.android.pdk.PDKCallback
import com.pinterest.android.pdk.PDKClient
import com.pinterest.android.pdk.PDKException
import com.pinterest.android.pdk.PDKResponse
import com.pinterest.android.pdk.PDKUser
import com.pinterest.android.pdk.Utils
import com.squareup.picasso.Picasso


import java.util.HashMap


class SearchUserActivity : ActionBarActivity() {

    lateinit var searchBox: EditText
    lateinit var searchButton: Button
    lateinit var followButton: Button
    lateinit var userImageView: ImageView
    lateinit var userName: TextView

    internal var user: PDKUser? = null
    private val USER_FIELDS = "id,image,first_name,last_name"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)

        searchBox = findViewById(R.id.search_user) as EditText

        searchButton = findViewById(R.id.search_button) as Button
        searchButton.setOnClickListener { onSearch() }

        userName = findViewById(R.id.user_name) as TextView
        userName.visibility = View.GONE

        userImageView = findViewById(R.id.user_imageview) as ImageView
        userImageView.visibility = View.GONE

        followButton = findViewById(R.id.follow_button) as Button
        followButton.setOnClickListener { onFollow() }
        followButton.visibility = View.GONE
    }

    private fun onSearch() {
        if (!Utils.isEmpty(searchBox.text.toString())) {
            PDKClient.instance.getUser(searchBox.text.toString(), USER_FIELDS, object : PDKCallback() {
                override fun onSuccess(response: PDKResponse) {
                    Log.d(javaClass.name, "Response: " + response.status)
                    user = response.user
                    showUser()
                }

                override fun onFailure(exception: PDKException) {
                    Log.e(javaClass.name, "error: " + exception.detailMessage)
                }
            }
            )
        }
    }

    private fun onFollow() {
        val path = "me/following/users/"
        val param = HashMap<String, String>()
        param.put("user", user!!.uid!!)
        PDKClient.instance.postPath(path, param, object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                Log.d(javaClass.name, "Response: " + response.data!!.toString())
                Toast.makeText(this@SearchUserActivity, "User Follow success", Toast.LENGTH_SHORT)
                        .show()
            }

            override fun onFailure(exception: PDKException) {
                Log.e(javaClass.name, "error: " + exception.detailMessage)
                Toast.makeText(this@SearchUserActivity, "User Follow failed", Toast.LENGTH_SHORT)
                        .show()
            }
        })
    }

    private fun showUser() {
        if (user != null) {
            followButton.visibility = View.VISIBLE
            userImageView.visibility = View.VISIBLE
            userName.visibility = View.VISIBLE

            userName.text = user!!.firstName + " " + user!!.lastName
            Picasso.with(this).load(user!!.imageUrl).into(userImageView)
        }
    }
}
