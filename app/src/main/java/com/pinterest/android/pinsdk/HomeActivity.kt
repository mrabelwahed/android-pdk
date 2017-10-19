package com.pinterest.android.pinsdk

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.View
import android.widget.Button
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


class HomeActivity : ActionBarActivity() {
    lateinit var pinsButton: Button
    lateinit var boardsButton: Button
    lateinit var followingButton: Button
    lateinit var logoutButton: Button
    lateinit var pathButton: Button
    lateinit var nameTv: TextView
    lateinit var profileIv: ImageView
    private val USER_FIELDS = "id,image,counts,created_at,first_name,last_name,bio"
    internal var user: PDKUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        title = "Pinterest SDK Demo"

        nameTv = findViewById(R.id.name_textview) as TextView
        profileIv = findViewById(R.id.profile_imageview) as ImageView

        pinsButton = findViewById(R.id.pins_button) as Button
        pinsButton.setOnClickListener { onMyPins() }

        boardsButton = findViewById(R.id.boards_button) as Button
        boardsButton.setOnClickListener { onMyBoards() }

        followingButton = findViewById(R.id.following_button) as Button
        followingButton.setOnClickListener { onMyFollowing() }

        pathButton = findViewById(R.id.path_button) as Button
        pathButton.setOnClickListener { onGetPath() }

        logoutButton = findViewById(R.id.logout_button) as Button
        logoutButton.setOnClickListener { onLogout() }

        getMe()
    }

    private fun setUser(user: PDKUser) {
        nameTv.text = user?.firstName + " " + user?.lastName
        Picasso.with(this).load(user.imageUrl).into(profileIv)
    }

    private fun getMe() {
        PDKClient.instance.getMe(USER_FIELDS, object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                if (DEBUG) log(String.format("status: %d", response.status))
                user = response.user
                setUser(response.user)
            }

            override fun onFailure(exception: PDKException) {
                if (DEBUG) log(exception.detailMessage)
                Toast.makeText(this@HomeActivity, "/me Request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onMyPins() {
        val i = Intent(this, MyPinsActivity::class.java)
        startActivity(i)
    }

    private fun onMyBoards() {
        val i = Intent(this, MyBoardsActivity::class.java)
        startActivity(i)
    }

    private fun onGetPath() {
        val i = Intent(this, AnyPathActivity::class.java)
        startActivity(i)
    }

    private fun onMyFollowing() {
        val i = Intent(this, FollowingActivity::class.java)
        startActivity(i)
    }

    private fun onLogout() {
        PDKClient.instance.logout()
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun showDialog(raw: String) {
        val frag = RawResponseDialogFragment.newInstance(raw)
        frag.show(supportFragmentManager, "RawResponseDialogFragment")
    }

    private fun log(msg: String) {
        if (!Utils.isEmpty(msg))
            Log.d(javaClass.name, msg)
    }

    companion object {

        private val DEBUG = true
    }
}
