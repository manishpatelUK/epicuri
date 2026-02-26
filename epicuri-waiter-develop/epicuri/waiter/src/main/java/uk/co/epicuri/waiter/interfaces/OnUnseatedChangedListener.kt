package uk.co.epicuri.waiter.interfaces

import uk.co.epicuri.waiter.model.EpicuriCustomer
import uk.co.epicuri.waiter.model.EpicuriParty
import java.util.ArrayList

interface OnUnseatedChangedListener {
    fun setParties(allParties: MutableList<EpicuriParty>?)
    fun setCheckins(payload: ArrayList<EpicuriCustomer.Checkin>?)
    fun getCheckedItemPosition(): Int
    fun setItemChecked(checkedItem: Int, b: Boolean)
    fun finishActionMode()
}