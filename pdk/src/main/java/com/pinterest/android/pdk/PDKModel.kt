package com.pinterest.android.pdk

abstract class PDKModel {
    // This is an optional right now because an id is not guaranteed to be returned
    // Will force this at the network layer in a followup
    abstract val uid: String?
}
