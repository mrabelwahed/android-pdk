package com.pinterest.android.pdk

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.apache.http.NameValuePair

import org.apache.http.message.BasicNameValuePair
import org.json.JSONException
import org.json.JSONObject


import java.io.UnsupportedEncodingException
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

class PDKClient private constructor() {
    private var authCallback: PDKCallback? = null

    /**
     * The oauth Token returned from the server upon authentication. If you store this in your
     * app, please be sure to do so securely and be warned that it can expire
     * at any time. If the token expires, you will need to re-authenticate and get a new token.
     */
    // ================================================================================
    // API Interface
    // ================================================================================

    /**
     * Set Oauth Access token
     */
    var accessToken: String?
        get() = Companion.accessToken
        set(token) {
            Companion.accessToken = token
            saveAccessToken(accessToken)
        }

    fun logout() {
        accessToken = null
        scopes = null
        cancelPendingRequests()
        saveAccessToken(null)
        saveScopes(null)
    }

    fun login(context: Context, permissions: List<String>, callback: PDKCallback?) {
        authCallback = callback
        if (Utils.isEmpty(permissions)) {
            callback?.onFailure(PDKException("Scopes cannot be empty"))
            return
        }
        if (context !is Activity) {
            callback?.onFailure(PDKException("Please pass Activity context with login request"))
            return
        }
        requestedScopes = HashSet<String>()
        requestedScopes!!.addAll(permissions)
        if (!Utils.isEmpty(accessToken) && !Utils.isEmpty(scopes)) {
            getPath("oauth/inspect", null, object : PDKCallback() {
                override fun onSuccess(response: PDKResponse) {
                    if (verifyAccessToken(response.data!!)) {
                        isAuthenticated = true
                        PDKClient.instance.getMe(authCallback!!)
                    } else {
                        initiateLogin(context, permissions)
                    }
                }

                override fun onFailure(exception: PDKException) {
                    initiateLogin(context, permissions)
                }
            })
        } else {
            initiateLogin(context, permissions)
        }
    }

    fun onOauthResponse(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PDKCLIENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Utils.log("PDK: result - %s", data.getStringExtra(PDKCLIENT_EXTRA_RESULT))
                onOauthResponse(data.getStringExtra(PDKCLIENT_EXTRA_RESULT))
            } else {
                Utils.log("PDK: Authentication failed")
                authCallback!!.onFailure(PDKException("Authentication failed"))
            }
        }
    }

    fun onConnect(context: Context) {
        if (context !is Activity) {
            if (authCallback != null) authCallback!!.onFailure(PDKException("Please pass Activity context with onConnect request"))
            return
        }
        val activity = context
        if (Intent.ACTION_VIEW == activity.intent.action) {
            val uri = activity.intent.data
            if (uri != null && uri.toString().contains("pdk$clientId://"))
                onOauthResponse(uri.toString())
        }
    }

    fun getPath(path: String, callback: PDKCallback) {
        getPath(path, null, callback)
    }

    fun getPath(path: String, params: HashMap<String, String>?, callback: PDKCallback?) {
        var params = params
        if (Utils.isEmpty(path)) {
            callback?.onFailure(PDKException("Invalid path"))
            return
        }
        val url = PROD_BASE_API_URL + path
        if (params == null) params = HashMap<String, String>()
        callback?.setPath(path)
        callback?.setParams(params)
        getRequest(url, params, callback)
    }

    fun postPath(path: String, params: HashMap<String, String>, callback: PDKCallback?) {
        if (Utils.isEmpty(path)) {
            callback?.onFailure(PDKException("Invalid path"))
            return
        }
        callback?.setPath(path)
        val url = PROD_BASE_API_URL + path
        postRequest(url, params, callback)
    }

    fun deletePath(path: String, callback: PDKCallback?) {
        if (Utils.isEmpty(path)) {
            callback?.onFailure(PDKException("Invalid path"))
            return
        }
        callback?.setPath(path)
        val url = PROD_BASE_API_URL + path
        deleteRequest(url, null, callback)
    }

    fun putPath(path: String, params: HashMap<String, String>, callback: PDKCallback?) {
        if (Utils.isEmpty(path)) {
            callback?.onFailure(PDKException("Invalid path"))
            return
        }
        callback?.setPath(path)
        val url = PROD_BASE_API_URL + path
        putRequest(url, params, callback)
    }


    //Authorized user Endpoints

    fun getMe(callback: PDKCallback) {
        getPath(ME, callback)
    }

    fun getMe(fields: String, callback: PDKCallback) {
        getPath(ME, getMapWithFields(fields), callback)
    }

    fun getMyPins(fields: String, callback: PDKCallback) {
        val path = ME + PINS
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getMyBoards(fields: String, callback: PDKCallback) {
        val path = ME + BOARDS
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getMyLikes(fields: String, callback: PDKCallback) {
        val path = ME + LIKES
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getMyFollowers(fields: String, callback: PDKCallback) {
        val path = ME + FOLLOWERS
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getMyFollowedUsers(fields: String, callback: PDKCallback) {
        val path = ME + FOLLOWING + USER
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getMyFollowedBoards(fields: String, callback: PDKCallback) {
        val path = ME + FOLLOWING + BOARDS
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getMyFollowedInterests(fields: String, callback: PDKCallback) {
        val path = ME + FOLLOWING + INTERESTS
        getPath(path, getMapWithFields(fields), callback)
    }

    //User Endpoint

    fun getUser(userId: String, fields: String, callback: PDKCallback?) {
        if (Utils.isEmpty(userId)) {
            callback?.onFailure(PDKException("Invalid user name/Id"))
            return
        }
        val path = USER + userId
        getPath(path, getMapWithFields(fields), callback)
    }

    //Board Endpoints

    fun getBoard(boardId: String, fields: String, callback: PDKCallback?) {
        if (Utils.isEmpty(boardId)) {
            callback?.onFailure(PDKException("Invalid board Id"))
            return
        }
        val path = BOARDS + boardId
        getPath(path, getMapWithFields(fields), callback)
    }

    fun getBoardPins(boardId: String, fields: String, callback: PDKCallback?) {
        if (Utils.isEmpty(boardId)) {
            callback?.onFailure(PDKException("Invalid board Id"))
            return
        }
        val path = BOARDS + boardId + "/" + PINS
        getPath(path, getMapWithFields(fields), callback)
    }

    fun deleteBoard(boardId: String, callback: PDKCallback?) {
        if (Utils.isEmpty(boardId)) {
            callback?.onFailure(PDKException("Board Id cannot be empty"))
        }
        val path = BOARDS + boardId + "/"
        deletePath(path, callback)
    }

    fun createBoard(name: String, desc: String, callback: PDKCallback?) {
        if (Utils.isEmpty(name)) {
            callback?.onFailure(PDKException("Board name cannot be empty"))
            return
        }
        val params = HashMap<String, String>()
        params.put("name", name)
        if (!Utils.isEmpty(desc)) params.put("description", desc)
        postPath(BOARDS, params, callback)
    }

    //Pin Endpoints

    fun getPin(pinId: String, fields: String, callback: PDKCallback?) {
        if (Utils.isEmpty(pinId)) {
            callback?.onFailure(PDKException("Invalid pin Id"))
            return
        }
        val path = PINS + pinId
        getPath(path, getMapWithFields(fields), callback)
    }

    fun createPin(note: String, boardId: String, imageUrl: String, link: String, callback: PDKCallback?) {
        if (Utils.isEmpty(note) || Utils.isEmpty(boardId) || Utils.isEmpty(imageUrl)) {
            callback?.onFailure(PDKException("Board Id, note, Image cannot be empty"))
            return
        }
        val params = HashMap<String, String>()
        params.put("board", boardId)
        params.put("note", note)
        if (!Utils.isEmpty(link)) params.put("link", link)
        if (!Utils.isEmpty(imageUrl)) params.put("image_url", imageUrl)
        postPath(PINS, params, callback)
    }

    fun deletePin(pinId: String, callback: PDKCallback?) {
        if (Utils.isEmpty(pinId)) {
            callback?.onFailure(PDKException("Pin Id cannot be empty"))
        }
        val path = PINS + pinId + "/"
        deletePath(path, callback)
    }


    // ================================================================================
    // Internal
    // ================================================================================

    private fun onOauthResponse(result: String) {
        if (!Utils.isEmpty(result)) {
            val uri = Uri.parse(result)
            if (uri.getQueryParameter("access_token") != null) {
                var token = uri.getQueryParameter("access_token")
                try {
                    token = java.net.URLDecoder.decode(token, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    Utils.loge(e.localizedMessage)
                }

                accessToken = token
                isAuthenticated = true
                PDKClient.instance.getMe(authCallback!!)
                saveAccessToken(accessToken)
            }
            if (uri.getQueryParameter("error") != null) {
                val error = uri.getQueryParameter("error")
                Utils.loge("PDK: authentication error: %s", error)
            }
        }
        if (accessToken == null)
            authCallback!!.onFailure(PDKException("PDK: authentication failed"))
    }

    private fun initiateLogin(c: Context, permissions: List<String>) {
        if (pinterestInstalled(c)) {
            val intent = createAuthIntent(c, clientId!!, permissions)
            openPinterestAppForLogin(c, intent, permissions)
        } else {
            initiateWebLogin(c, permissions)
        }
    }

    private fun initiateWebLogin(c: Context, permissions: List<String>) {
        try {
            val paramList = LinkedList<NameValuePair>()
            paramList.add(BasicNameValuePair("client_id", clientId))
            paramList.add(BasicNameValuePair("scope", TextUtils.join(",", permissions)))
            paramList.add(BasicNameValuePair("redirect_uri", "pdk$clientId://"))
            paramList.add(BasicNameValuePair("response_type", "token"))

            val url = Utils.getUrlWithQueryParams(PROD_WEB_OAUTH_URL, paramList)
            val oauthIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            c.startActivity(oauthIntent)

        } catch (e: Exception) {
            Utils.loge("PDK: Error initiating web oauth")
        }

    }

    private fun openPinterestAppForLogin(c: Context, intent: Intent, permissions: List<String>) {
        try {
            //Utils.log("PDK: starting Pinterest app for auth");
            (c as Activity).startActivityForResult(intent, PDKCLIENT_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            // Ideally this should not happen because intent is not null
            Utils.loge("PDK: failed to open Pinterest App for login")
            initiateWebLogin(c, permissions)
            return
        }

        return
    }

    private fun createAuthIntent(context: Context, appId: String, permissions: List<String>): Intent {
        return Intent()
                .setClassName(PINTEREST_PACKAGE, PINTEREST_OAUTH_ACTIVITY)
                .putExtra(PDKCLIENT_EXTRA_APPID, appId)
                .putExtra(PDKCLIENT_EXTRA_APPNAME, "appName")
                .putExtra(PDKCLIENT_EXTRA_PERMISSIONS, TextUtils.join(",", permissions))
    }

    private fun saveAccessToken(accessToken: String?) {
        val sharedPref = context!!.getSharedPreferences(PDK_SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(PDK_SHARED_PREF_TOKEN_KEY, accessToken)
        editor.commit()
    }

    private fun saveScopes(perms: Set<String>?) {
        val sharedPref = context!!.getSharedPreferences(PDK_SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putStringSet(PDK_SHARED_PREF_SCOPES_KEY, perms)
        editor.commit()
    }

    private fun getMapWithFields(fields: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        map.put(PDK_QUERY_PARAM_FIELDS, fields)
        return map
    }

    private fun verifyAccessToken(obj: Any): Boolean {
        var verified = false
        var appId = ""
        val appScopes = HashSet<String>()
        try {
            val jsonObject = obj as JSONObject
            if (jsonObject.has("app")) {
                val appObj = jsonObject.getJSONObject("app")
                if (appObj.has("id")) {
                    appId = appObj.getString("id")
                }
            }
            if (jsonObject.has("scopes")) {
                val scopesArray = jsonObject.getJSONArray("scopes")
                val size = scopesArray.length()
                for (i in 0..size - 1) {
                    appScopes.add(scopesArray.get(i).toString())
                }
            }
        } catch (exception: JSONException) {
            Utils.loge("PDK: ", exception.localizedMessage)
        }

        if (!Utils.isEmpty(appScopes)) {
            saveScopes(appScopes)
        }
        if (!Utils.isEmpty(appId) && !Utils.isEmpty(appScopes)) {
            if (appId.equals(clientId!!, ignoreCase = true) && appScopes == requestedScopes) {
                verified = true
            }
        }
        return verified
    }

    companion object {

        val PDKCLIENT_VERSION_CODE = "1.0"

        val PDKCLIENT_PERMISSION_READ_PUBLIC = "read_public"
        val PDKCLIENT_PERMISSION_WRITE_PUBLIC = "write_public"
        val PDKCLIENT_PERMISSION_READ_RELATIONSHIPS = "read_relationships"
        val PDKCLIENT_PERMISSION_WRITE_RELATIONSHIPS = "write_relationships"

        val PDK_QUERY_PARAM_FIELDS = "fields"
        val PDK_QUERY_PARAM_CURSOR = "cursor"

        private val PDKCLIENT_EXTRA_APPID = "PDKCLIENT_EXTRA_APPID"
        private val PDKCLIENT_EXTRA_APPNAME = "PDKCLIENT_EXTRA_APPNAME"
        private val PDKCLIENT_EXTRA_PERMISSIONS = "PDKCLIENT_EXTRA_PERMISSIONS"
        private val PDKCLIENT_EXTRA_RESULT = "PDKCLIENT_EXTRA_RESULT"

        private val PDK_SHARED_PREF_FILE_KEY = "com.pinterest.android.pdk.PREF_FILE_KEY"
        private val PDK_SHARED_PREF_TOKEN_KEY = "PDK_SHARED_PREF_TOKEN_KEY"
        private val PDK_SHARED_PREF_SCOPES_KEY = "PDK_SHARED_PREF_SCOPES_KEY"
        private val PDKCLIENT_REQUEST_CODE = 8772
        private val VOLLEY_TAG = "volley_tag"

        private val PROD_BASE_API_URL = "https://api.pinterest.com/v1/"
        private val PROD_WEB_OAUTH_URL = "https://api.pinterest.com/oauth/"
        private val ME = "me/"
        private val USER = "users/"
        private val PINS = "pins/"
        private val BOARDS = "boards/"
        private val LIKES = "likes/"
        private val FOLLOWERS = "followers/"
        private val FOLLOWING = "following/"
        private val INTERESTS = "interests/"


        // ================================================================================
        // Getters/Setters
        // ================================================================================

        /**
         * Get state of debug mode

         * @return true if enabled, false if disabled
         */
        /**
         * Enable/disable debug mode which will print logs when there are issues.

         * @param debugMode true to enabled, false to disable
         */
        var isDebugMode: Boolean = false
        private var clientId: String? = null
        private var context: Context? = null
        private var accessToken: String? = null
        private var scopes: Set<String>? = null
        private var requestedScopes: MutableSet<String>? = null
        private var mInstance: PDKClient? = null
        private var _requestQueue: RequestQueue? = null

        private var isConfigured: Boolean = false
        private var isAuthenticated = false

        private val PINTEREST_PACKAGE = "com.pinterest"
        private val PINTEREST_OAUTH_ACTIVITY = "com.pinterest.sdk.PinterestOauthActivity"
        private val PINTEREST_SIGNATURE_HASH = "b6a74dbcb894b0f73d8c485c72eb1247a8f027ca"

        val instance: PDKClient
            get() {
                var instance = mInstance
                if (instance == null) {
                    instance = PDKClient()
                    mInstance = instance
                    _requestQueue = requestQueue
                }
                return instance
            }

        fun configureInstance(context: Context, clientId: String): PDKClient {
            PDKClient.clientId = clientId
            PDKClient.context = context.applicationContext
            isConfigured = true

            accessToken = restoreAccessToken()
            scopes = restoreScopes()
            isAuthenticated = accessToken != null
            return PDKClient.instance
        }


        //    //validate Pinterest Activity and/or package integrity
        //    private static Intent validateActivityIntent(Context context, Intent intent) {
        //        if (intent == null) {
        //            return null;
        //        }
        //
        //        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
        //        if (resolveInfo == null) {
        //            return null;
        //        }
        //
        //        //validate pinterest app?
        //        //        if (!appInfo.validateSignature(context, resolveInfo.activityInfo.packageName)) {
        //        //            return null;
        //        //        }
        //
        //        return intent;
        //    }

        /**
         * Check if the device meets the requirements needed to pin using this library.

         * @return true for supported, false otherwise
         */
        private fun meetsRequirements(): Boolean {
            return Build.VERSION.SDK_INT >= 8
        }

        /**
         * Check if the device has Pinterest installed that supports PinIt Button

         * @param context Application or Activity context
         * *
         * @return true if requirements are met, false otherwise
         */
        private fun pinterestInstalled(context: Context): Boolean {
            if (!meetsRequirements())
                return false

            var installed = false
            try {
                val info = context.packageManager.getPackageInfo(PINTEREST_PACKAGE, PackageManager.GET_SIGNATURES)
                if (info != null && info.versionCode >= 16) {
                    //Utils.log("PDK versionCode:%s versionName:%s", info.versionCode,
                    //    info.versionName);
                    for (signature in info.signatures) {
                        val signatureHash = Utils.sha1Hex(signature.toByteArray())
                        installed = signatureHash == PINTEREST_SIGNATURE_HASH
                    }
                }
                if (!installed)
                    Utils.log("PDK: Pinterest App not installed or version too low!")
            } catch (e: Exception) {
                Utils.loge(e.localizedMessage)
                installed = false
            }

            return installed
        }

        private fun restoreAccessToken(): String? {
            val sharedPref = context!!.getSharedPreferences(PDK_SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE)
            return sharedPref.getString(PDK_SHARED_PREF_TOKEN_KEY, null)
        }

        private fun restoreScopes(): Set<String>? {
            val sharedPref = context!!.getSharedPreferences(PDK_SHARED_PREF_FILE_KEY, Context.MODE_PRIVATE)
            return sharedPref.getStringSet(PDK_SHARED_PREF_SCOPES_KEY, HashSet<String>())
        }

        private val requestQueue: RequestQueue
            get() {
                var requestQueue = _requestQueue
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(context!!)!!
                    _requestQueue = requestQueue
                }
                return requestQueue
            }

        private fun <T> addToRequestQueue(req: Request<T>) {
            req.tag = VOLLEY_TAG
            requestQueue.add(req)
        }

        private fun cancelPendingRequests() {
            _requestQueue!!.cancelAll(VOLLEY_TAG)
        }

        private fun validateScopes(requestedScopes: Set<String>): Boolean {
            return scopes == requestedScopes
        }

        private val headers: Map<String, String>
            get() {
                val headers = HashMap<String, String>()
                headers.put("User-Agent", String.format("PDK %s", PDKCLIENT_VERSION_CODE))
                return headers
            }

        private fun getRequest(url: String, params: HashMap<String, String>, callback: PDKCallback?): Request<*> {
            var url = url
            var callback = callback
            Utils.log("PDK GET: %s", url)
            val paramList = LinkedList<NameValuePair>()
            paramList.add(BasicNameValuePair("access_token", accessToken))
            if (!Utils.isEmpty(params)) {
                for ((key, value) in params) {
                    paramList.add(BasicNameValuePair(key, value))
                }
            }
            url = Utils.getUrlWithQueryParams(url, paramList)!!

            if (callback == null) callback = PDKCallback()
            val request = PDKRequest(Request.Method.GET, url, null, callback, headers)
            addToRequestQueue(request)
            return request
        }

        private fun postRequest(url: String, params: HashMap<String, String>?, callback: PDKCallback?): Request<*> {
            var url = url
            var params = params
            var callback = callback
            Utils.log(String.format("PDK POST: %s", url))
            if (params == null) params = HashMap<String, String>()

            val queryParams = LinkedList<NameValuePair>()
            queryParams.add(BasicNameValuePair("access_token", accessToken))
            url = Utils.getUrlWithQueryParams(url, queryParams)!!

            if (callback == null) callback = PDKCallback()
            val request = PDKRequest(Request.Method.POST, url, JSONObject(params), callback, headers)
            addToRequestQueue(request)
            return request
        }

        private fun deleteRequest(url: String, params: HashMap<String, String>?, callback: PDKCallback?): Request<*> {
            var url = url
            var callback = callback
            Utils.log(String.format("PDK DELETE: %s", url))

            val queryParams = LinkedList<NameValuePair>()
            queryParams.add(BasicNameValuePair("access_token", accessToken))
            url = Utils.getUrlWithQueryParams(url, queryParams)!!

            if (callback == null) callback = PDKCallback()

            val request = PDKRequest(Request.Method.DELETE, url, null, callback, headers)
            request.setShouldCache(false)
            addToRequestQueue(request)
            return request
        }

        private fun putRequest(url: String, params: HashMap<String, String>?, callback: PDKCallback?): Request<*> {
            var url = url
            var params = params
            var callback = callback
            Utils.log(String.format("PDK PUT: %s", url))
            if (params == null) params = HashMap<String, String>()

            val queryParams = LinkedList<NameValuePair>()
            queryParams.add(BasicNameValuePair("access_token", accessToken))
            url = Utils.getUrlWithQueryParams(url, queryParams)!!

            if (callback == null) callback = PDKCallback()
            val request = PDKRequest(Request.Method.PUT, url, JSONObject(params), callback, headers)
            addToRequestQueue(request)
            return request
        }
    }
}
