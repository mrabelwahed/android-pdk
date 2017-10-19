package com.pinterest.android.pinsdk

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * Use the [RawResponseDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RawResponseDialogFragment : DialogFragment() {
    private var raw: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            raw = arguments.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_raw_response_dialog, container, false)
    }

    override fun onActivityCreated(b: Bundle?) {
        super.onActivityCreated(b)
        val tv = view!!.findViewById(R.id.tv) as TextView
        tv.movementMethod = ScrollingMovementMethod()
        tv.text = raw
    }

    companion object {

        private val ARG_PARAM1 = "raw"

        fun newInstance(raw: String): RawResponseDialogFragment {
            val fragment = RawResponseDialogFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, raw)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
