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
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView

import com.pinterest.android.pdk.PDKCallback
import com.pinterest.android.pdk.PDKClient
import com.pinterest.android.pdk.PDKException
import com.pinterest.android.pdk.PDKPin
import com.pinterest.android.pdk.PDKResponse
import com.squareup.picasso.Picasso
import java.util.ArrayList

class MyPinsActivity : ActionBarActivity() {

    private var myPinsCallback: PDKCallback? = null
    private var myPinsResponse: PDKResponse? = null
    private var _gridView: GridView? = null
    private var _pinAdapter: PinsAdapter? = null
    private var _loading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pins)
        title = "My Pins"
        _pinAdapter = PinsAdapter(this)
        _gridView = findViewById(R.id.grid_view) as GridView

        _gridView!!.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val inflater = menuInflater
            inflater.inflate(R.menu.context_menu_boards, menu)
        }
        _gridView!!.adapter = _pinAdapter
        myPinsCallback = object : PDKCallback() {
            override fun onSuccess(response: PDKResponse) {
                _loading = false
                myPinsResponse = response
                _pinAdapter!!.pinList = response.pinList
            }

            override fun onFailure(exception: PDKException) {
                _loading = false
                Log.e(javaClass.name, exception.detailMessage)
            }
        }
        _loading = true
        fetchPins()
    }

    override fun onResume() {
        super.onResume()
        fetchPins()
    }

    private fun fetchPins() {
        _pinAdapter!!.pinList = null
        PDKClient.instance.getMyPins(PIN_FIELDS, myPinsCallback!!)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        when (item.itemId) {
            R.id.action_board_delete -> {
                deletePin(info.position)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_my_pins, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_pin -> {
                createNewPin()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun createNewPin() {
        val i = Intent(this, CreatePinActivity::class.java)
        startActivity(i)
    }

    private fun deletePin(position: Int) {
        PDKClient.instance.deletePin(_pinAdapter!!.pinList!![position].uid!!,
                object : PDKCallback() {
                    override fun onSuccess(response: PDKResponse) {
                        Log.d(javaClass.name, "Response: " + response.status)
                        fetchPins()
                    }

                    override fun onFailure(exception: PDKException) {
                        Log.e(javaClass.name, "error: " + exception.detailMessage)
                    }
                })
    }

    private fun loadNext() {
        if (!_loading && myPinsResponse!!.hasNext()) {
            _loading = true
            myPinsResponse!!.loadNext(myPinsCallback!!)
        }
    }

    private inner class PinsAdapter(private val _context: Context) : BaseAdapter() {

        private var _pinList: MutableList<PDKPin>? = null

        var pinList: List<PDKPin>?
            get() = _pinList
            set(list) {
                if (_pinList == null) _pinList = ArrayList<PDKPin>()
                if (list == null)
                    _pinList!!.clear()
                else
                    _pinList!!.addAll(list)
                notifyDataSetChanged()
            }

        override fun getCount(): Int {
            return if (_pinList == null) 0 else _pinList!!.size
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
            if (_pinList!!.size - position < 5) {
                loadNext()
            }

            if (convertView == null) {
                val inflater = (_context as Activity).layoutInflater
                convertView = inflater.inflate(R.layout.list_item_pin, parent, false)

                viewHolder = ViewHolderItem()
                viewHolder.textViewItem = convertView!!.findViewById(R.id.title_view) as TextView
                viewHolder.imageView = convertView.findViewById(R.id.image_view) as ImageView

                convertView.tag = viewHolder

            } else {
                viewHolder = convertView.tag as ViewHolderItem
            }

            val pinItem = _pinList!![position]
            if (pinItem != null) {
                viewHolder.textViewItem!!.text = pinItem.note
                Picasso.with(_context.applicationContext).load(pinItem.imageUrl).into(viewHolder.imageView)
            }

            return convertView
        }

        private inner class ViewHolderItem {
            internal var textViewItem: TextView? = null
            internal var imageView: ImageView? = null
        }
    }

    companion object {
        private val PIN_FIELDS = "id,link,creator,image,counts,note,created_at,board,metadata"
    }
}
