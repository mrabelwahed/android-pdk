package com.pinterest.android.pinsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

import com.pinterest.android.pdk.PDKBoard
import com.pinterest.android.pdk.PDKCallback
import com.pinterest.android.pdk.PDKClient
import com.pinterest.android.pdk.PDKException
import com.pinterest.android.pdk.PDKResponse


import java.util.ArrayList


class MyBoardsActivity : ActionBarActivity() {

    private var myBoardsCallback: PDKCallback? = null
    private var myBoardsResponse: PDKResponse? = null
    private var _listView: ListView? = null
    private var _boardsAdapter: BoardsAdapter? = null
    private var _loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_boards)
        title = "My Boards"
        _boardsAdapter = BoardsAdapter(this)
        _listView = findViewById(R.id.listView) as ListView

        _listView!!.adapter = _boardsAdapter
        _listView!!.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val inflater = menuInflater
            inflater.inflate(R.menu.context_menu_boards, menu)
        }

        myBoardsCallback = object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                _loading = false
                myBoardsResponse = response
                _boardsAdapter!!.boardList = response.boardList
            }

            override fun onFailure(exception: PDKException) {
                _loading = false
                Log.e(javaClass.name, exception.detailMessage)
            }
        }
        _loading = true
    }

    private fun fetchBoards() {
        _boardsAdapter!!.boardList = null
        PDKClient.instance.getMyBoards(BOARD_FIELDS, myBoardsCallback!!)
    }

    private fun loadNext() {
        if (!_loading && myBoardsResponse!!.hasNext()) {
            _loading = true
            myBoardsResponse!!.loadNext(myBoardsCallback!!)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchBoards()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        when (item.itemId) {
            R.id.action_board_delete -> {
                deleteBoard(info.position)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_my_boards, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_board -> {
                createNewBoard()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun createNewBoard() {
        val i = Intent(this, CreateBoardActivity::class.java)
        startActivity(i)
    }

    private fun deleteBoard(position: Int) {
        PDKClient.instance.deleteBoard(_boardsAdapter!!.boardList!![position].uid!!, object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                Log.d(javaClass.name, "Response: " + response.status)
                fetchBoards()
            }

            override fun onFailure(exception: PDKException) {
                Log.e(javaClass.name, "error: " + exception.detailMessage)
            }
        })
    }

    private inner class BoardsAdapter(private val _context: Context) : BaseAdapter() {

        private var _boardList: MutableList<PDKBoard>? = null

        var boardList: List<PDKBoard>?
            get() = _boardList
            set(list) {
                if (_boardList == null) _boardList = ArrayList<PDKBoard>()
                if (list == null)
                    _boardList!!.clear()
                else
                    _boardList!!.addAll(list)
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return if (_boardList == null) 0 else _boardList!!.size
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
            if (_boardList!!.size - position < 5) {
                loadNext()
            }

            if (convertView == null) {
                val inflater = (_context as Activity).layoutInflater
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)

                viewHolder = ViewHolderItem()
                viewHolder.textViewItem = convertView!!.findViewById(android.R.id.text1) as TextView

                convertView.tag = viewHolder

            } else {
                viewHolder = convertView.tag as ViewHolderItem
            }

            val boardItem = _boardList!![position]
            if (boardItem != null) {
                viewHolder.textViewItem!!.text = boardItem.name
            }

            return convertView
        }

        private inner class ViewHolderItem {
            internal var textViewItem: TextView? = null
        }
    }

    companion object {
        private val BOARD_FIELDS = "id,name,description,creator,image,counts,created_at"
    }
}
