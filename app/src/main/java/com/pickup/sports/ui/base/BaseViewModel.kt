package com.pickup.sports.ui.base


import android.view.View
import androidx.lifecycle.ViewModel
import com.pickup.sports.utils.event.SingleActionEvent
import okhttp3.ResponseBody
import org.json.JSONObject

open class BaseViewModel : ViewModel() {

    val onClick: SingleActionEvent<View?> = SingleActionEvent()

    val onUnAuth: SingleActionEvent<Int?> = SingleActionEvent()
    override fun onCleared() {
        super.onCleared()
    }

    open fun onClick(view: View?) {
        onClick.value = view
    }



//    fun handleErrorResponse(errorBody: ResponseBody?): String {
//        if (errorBody != null) {
//            try {
//                val errorString = errorBody.string()
//                val jsonObject = JSONObject(errorString)
//                if (jsonObject.has("message")) {
//                    val errMsg = jsonObject.getString("message")
//                    return errMsg
//                }
//            } catch (e: JSONException) {
//                return "Something went wrong1"
//                // Handle the JSON parsing exception
//                e.printStackTrace()
//            }
//        }
//        return "Something went wrong"
//    }

    fun handleErrorResponse(errorBody: ResponseBody?, code: Int? = null): String {
        val text: String? = errorBody?.string()
        if (code != null && code == 401) {
            onUnAuth.postValue(code)
        }
        if (!text.isNullOrEmpty()) {
            return try {
                val obj = JSONObject(text)
                obj.getString("message")
            } catch (e: Exception) {
                return text
            }
        }
        return errorBody.toString()
    }
}