package com.pinterest.android.pinsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.pinterest.android.pdk.PDKCallback
import com.pinterest.android.pdk.PDKClient
import com.pinterest.android.pdk.PDKException
import com.pinterest.android.pdk.PDKResponse
import com.pinterest.android.pdk.PDKUser


import java.util.ArrayList


class FollowingActivity : ActionBarActivity() {

    private var myFollowingCallback: PDKCallback? = null
    private var myFollowingResponse: PDKResponse? = null
    private var _listView: ListView? = null
    private var _followingAdapter: FollowingAdapter? = null
    private var _loading = false
    private val USER_FIELDS = "id,image,counts,created_at,first_name,last_name,bio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)
        title = "My Following"
        _followingAdapter = FollowingAdapter(this)
        _listView = findViewById(R.id.listView) as ListView

        _listView!!.adapter = _followingAdapter

        myFollowingCallback = object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                _loading = false
                myFollowingResponse = response
                _followingAdapter!!.followingList = response.userList
            }

            override fun onFailure(exception: PDKException) {
                _loading = false
                Log.e(javaClass.name, exception.detailMessage)
            }
        }
        _loading = true
    }

    private fun fetchFollowers() {
        _followingAdapter!!.followingList = null
        PDKClient.instance.getPath("me/following/users/", myFollowingCallback!!)
    }

    private fun loadNext() {
        if (!_loading && myFollowingResponse!!.hasNext()) {
            _loading = true
            myFollowingResponse!!.loadNext(myFollowingCallback!!)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchFollowers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_following, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search_user -> {
                SearchUser()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun SearchUser() {
        val i = Intent(this, SearchUserActivity::class.java)
        startActivity(i)
    }

    private fun onUnfollowUser(position: Int) {
        val userId = _followingAdapter!!.followingList!![position].uid
        val path = "me/following/users/$userId/"
        PDKClient.instance.deletePath(path, object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                Log.d(javaClass.name, "Response: " + response.data!!.toString())
                Toast.makeText(this@FollowingActivity, "User Unfollow success", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(exception: PDKException) {
                Log.e(javaClass.name, "error: " + exception.detailMessage)
                Toast.makeText(this@FollowingActivity, "User Unfollow failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private inner class FollowingAdapter(private val _context: Context) : BaseAdapter() {

        private var _followingList: MutableList<PDKUser>? = null

        var followingList: List<PDKUser>?
            get() = _followingList
            set(list) {
                if (_followingList == null) _followingList = ArrayList<PDKUser>()
                if (list == null)
                    _followingList!!.clear()
                else
                    _followingList!!.addAll(list)
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return if (_followingList == null) 0 else _followingList!!.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val viewHolder: ViewHolderItem

            //load more pins if about to reach end of list
            if (_followingList!!.size - position < 5) {
                loadNext()
            }

            if (convertView == null) {
                val inflater = (_context as Activity).layoutInflater
                convertView = inflater.inflate(R.layout.list_item_following, parent, false)

                viewHolder = ViewHolderItem()
                viewHolder.textView = convertView!!.findViewById(R.id.text_view) as TextView
                viewHolder.unfollowButton = convertView.findViewById(R.id.unfollow_button) as Button

                convertView.tag = viewHolder

            } else {
                viewHolder = convertView.tag as ViewHolderItem
            }

            val user = _followingList!![position]
            if (user != null) {
                viewHolder.textView!!.text = user.firstName
                viewHolder.unfollowButton!!.setOnClickListener { onUnfollowUser(position) }
            }

            return convertView
        }

        private inner class ViewHolderItem {
            internal var textView: TextView? = null
            internal var unfollowButton: Button? = null
        }
    }

}
