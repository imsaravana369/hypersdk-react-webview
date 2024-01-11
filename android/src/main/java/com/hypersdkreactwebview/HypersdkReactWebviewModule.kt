package com.hypersdkreactwebview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import org.json.JSONArray
import org.json.JSONObject
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap

class HypersdkReactWebviewModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), ActivityEventListener {

  override fun getName(): String {
    return NAME
  }
  companion object {
    const val NAME = "UPIModule"
  }

  private  var resultCallback : Callback? = null;

  @ReactMethod
  fun findApps(payload : String?, callback : Callback){

    var result : List<Map<String, Any>> = ArrayList();
    currentActivity?.let {
      result = UPIInterface.findApps(it,payload) ;
      Log.d("girija findApps result", result.toString());
    }
    callback.invoke(Constants.FINDAPPS_REQUEST_CODE, null, convertListToWritableArray(result));
  }
  init{
    reactContext.addActivityEventListener(this);
  }

  @ReactMethod
  fun getWebviewVersion(callback : Callback){
    var result : String = "";
    currentActivity?.let {
      try {
        result = UPIInterface.getResourceByName(it, "hyper_webview_sdk_version");
      }catch (e : Exception){
        Log.e("HyperwebviewBridge", "Something went wrong ${e.toString()}");
      }
    }
    callback.invoke(Constants.GET_RESOURCE_NAME, null, result);
  }

  @ReactMethod
  fun openApp(packageName: String?, payload: String?, action: String?, flag: Int, callback: Callback ) {
    currentActivity?.let {
      try {
        UPIInterface.openApp(it,packageName,payload,action,flag);
        resultCallback = callback;
      }catch(e: Exception){ }
    }

  }

  // Translated from HYPER-WEBVIEW-Android
  private fun toJSON(bundle: Bundle?): JSONObject {
    val json = JSONObject()
    try {
      if (bundle != null) {
        val keys = bundle.keySet()
        for (key in keys) {
          val value = bundle[key]
          if (value == null) {
            json.put(key, JSONObject.NULL)
          } else if (value is ArrayList<*>) {
            json.put(key, toJSONArray(value))
          } else if (value is Bundle) {
            json.put(key, toJSON(value as Bundle?))
          } else {
            json.put(key, value.toString())
          }
        }
      }
    } catch (ignored: java.lang.Exception) {
    }
    return json
  }

  // Translated from HYPER-WEBVIEW-Android
  private fun toJSONArray(array: ArrayList<*>): JSONArray {
    val jsonArray = JSONArray()
    for (obj in array) {
      if (obj is ArrayList<*>) {
        jsonArray.put(toJSONArray(obj))
      } else if (obj is JSONObject) {
        jsonArray.put(obj)
      } else {
        jsonArray.put(obj.toString())
      }
    }
    return jsonArray
  }
  fun convertListToWritableArray(data: List<Map<String, Any>>): WritableArray {
    val writableArray: WritableArray = Arguments.createArray()

    for (map in data) {
      val writableMap: WritableMap = Arguments.createMap()

      for ((key, value) in map) {
        when (value) {
          is String -> writableMap.putString(key, value)
          is Int -> writableMap.putInt(key, value)
          is Double -> writableMap.putDouble(key, value)
          is Boolean -> writableMap.putBoolean(key, value)
          // Add more types as needed
          else -> writableMap.putString(key, value.toString())
        }
      }

      writableArray.pushMap(writableMap)
    }

    return writableArray
  }


  override fun onActivityResult(p0: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    Log.d("girija onAcitivtyResult1 :" , data.toString());
    if (requestCode != Constants.OPENAPPS_REQUEST_CODE) {
      return
    }

    Log.d("girija onAcitivtyResult :" , data.toString());
    val jsonObject: JSONObject = toJSON(data?.extras);
    val encoded = Base64.encodeToString(jsonObject.toString().toByteArray(), Base64.NO_WRAP);
    resultCallback?.invoke(requestCode, resultCode, encoded);
    resultCallback = null;
  }

  override fun onNewIntent(p0: Intent?) {
    TODO("Not yet implemented")
  }
}
