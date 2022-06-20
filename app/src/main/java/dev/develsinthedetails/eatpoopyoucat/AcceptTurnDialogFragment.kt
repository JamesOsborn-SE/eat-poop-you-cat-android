package dev.develsinthedetails.eatpoopyoucat

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

class AcceptTurnDialogFragment() : DialogFragment(), Parcelable {


    private lateinit var onClickListener: DialogInterface.OnClickListener

    // Use this instance of the interface to deliver action events
    private var otherPlayersName ="tets"
    constructor(parcel: Parcel) : this() {
        otherPlayersName = parcel.readString().toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherPlayersName = arguments!!.getString(PLAYER_NAME)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.dialog_accept_turn, otherPlayersName))
            .setPositiveButton(R.string.accept, onClickListener)
            .create()

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    fun setAcceptListener(acceptTurnDialogListener: DialogInterface.OnClickListener) {
        onClickListener = acceptTurnDialogListener
    }

    companion object CREATOR : Parcelable.Creator<AcceptTurnDialogFragment> {
        override fun createFromParcel(parcel: Parcel): AcceptTurnDialogFragment {
            return AcceptTurnDialogFragment(parcel)
        }
        const val TAG = "PurchaseConfirmationDialog"
        const val PLAYER_NAME="player name"
        override fun newArray(size: Int): Array<AcceptTurnDialogFragment?> {
            return arrayOfNulls(size)
        }
        @JvmStatic
        fun newInstance(playerName: String) =
            AcceptTurnDialogFragment().apply {
                arguments = bundleOf(PLAYER_NAME to playerName)
            }
    }



}
