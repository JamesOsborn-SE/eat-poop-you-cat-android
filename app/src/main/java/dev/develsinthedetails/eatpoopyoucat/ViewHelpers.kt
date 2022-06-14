package dev.develsinthedetails.eatpoopyoucat

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

class ViewHelpers {
}

fun Activity?.dismissKeyboard() {
    val inputMethodManager = this?.getSystemService( Context.INPUT_METHOD_SERVICE ) as InputMethodManager
    if( inputMethodManager.isAcceptingText && this.currentFocus != null)
        inputMethodManager.hideSoftInputFromWindow( this.currentFocus!!.windowToken, /*flags:*/ 0)
}