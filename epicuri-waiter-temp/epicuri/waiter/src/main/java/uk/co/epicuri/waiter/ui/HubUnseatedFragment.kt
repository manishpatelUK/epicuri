package uk.co.epicuri.waiter.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_hub_unseated.*
import uk.co.epicuri.waiter.R
import uk.co.epicuri.waiter.adapters.PartyAdapter
import uk.co.epicuri.waiter.interfaces.HubListener
import uk.co.epicuri.waiter.interfaces.OnUnseatedChangedListener
import uk.co.epicuri.waiter.model.EpicuriCustomer
import uk.co.epicuri.waiter.model.EpicuriParty
import uk.co.epicuri.waiter.utils.Utils
import java.util.ArrayList

class HubUnseatedFragment: Fragment(), AdapterView.OnItemClickListener, OnUnseatedChangedListener {
    private var unseatedListAdapter: PartyAdapter? = null
    private var checkinActionMode: ActionMode? = null
    private var hub: HubListener? = null
    private var partyToSeat: EpicuriParty? = null

    companion object {
        @JvmStatic
        fun newInstance(listener: HubListener): HubUnseatedFragment {
            val fragment = HubUnseatedFragment()
            fragment.hub = listener
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hub_unseated, container, false)
    }

    override fun onResume() {
        super.onResume()
        hub?.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unseatedListAdapter = PartyAdapter(context)
        unseatedList.adapter = unseatedListAdapter
        unseatedList.choiceMode = ListView.CHOICE_MODE_SINGLE
        unseatedList.onItemClickListener = this
        unseatedList.emptyView = unseatedListEmpty
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (unseatedListAdapter?.getItemType(position)) {
            PartyAdapter.Type.PARTY -> {
                checkinActionMode?.finish()
                partyToSeat = (activity as? HubActivity)?.partyToSeat
                unseatedList.setItemChecked(position, true)
                val newPartyToSeat = unseatedListAdapter?.getItem(position) as EpicuriParty
                if (newPartyToSeat == partyToSeat) {
                    if (partyToSeat?.getSessionId() != null && !(partyToSeat?.getSessionId() == "0" || partyToSeat?.getSessionId() == "-1")) {
                        hub?.launchSession(partyToSeat?.getSessionId())
                        (activity as? HubActivity)?.partyActionMode?.finish()
                    } else if (partyToSeat?.getReservationTime() != null) {
                        // this is a reservation
                        hub?.partyDetails()
                        hub?.editReservation(partyToSeat?.getId())
                        (activity as? HubActivity)?.partyActionMode?.finish()
                    }
                    return
                }

                (activity as? HubActivity)?.partyToSeat = newPartyToSeat
                partyToSeat = newPartyToSeat
                if (null == (activity as? HubActivity)?.partyActionMode) {
                    (activity as? HubActivity)?.partyActionMode = (activity as AppCompatActivity).startSupportActionMode((activity as HubActivity).SeatPartyActionMode())
                    (activity as? HubActivity)?.partyActionMode?.invalidate()
                    (activity as? HubActivity)?.isShowFloor = true
                    hub?.floorSwitch()
                } else {
                    (activity as? HubActivity)?.partyActionMode?.invalidate()
                }
                return
            }
            PartyAdapter.Type.CHECKIN -> {
                (activity as? HubActivity)?.partyActionMode?.finish()

                unseatedList.setItemChecked(position, true)
                (activity as? HubActivity)?.selectedCheckin = unseatedListAdapter?.getItem(position) as EpicuriCustomer.Checkin
                if (null == checkinActionMode) {
                    checkinActionMode = (activity as AppCompatActivity).startSupportActionMode(CheckinActionMode())
                    checkinActionMode?.invalidate()
                } else {
                    checkinActionMode?.invalidate()
                }
            }
        }
    }

    private inner class CheckinActionMode : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            activity?.menuInflater?.inflate(R.menu.action_checkin, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_newParty -> {
                    hub?.showNewPartyDialog(null)
                    checkinActionMode?.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            val currentItem = unseatedList.getCheckedItemPosition()
            if (AdapterView.INVALID_POSITION != currentItem) {
                // deselect current item
                unseatedList.setItemChecked(currentItem, false)
            }
            (activity as? HubActivity)?.selectedCheckin = null
            checkinActionMode = null
        }

    }

    override fun setParties(allParties: MutableList<EpicuriParty>?) {
        unseatedListAdapter?.setParties(allParties)
        Utils.reloadWaitingListSelection(unseatedListAdapter, unseatedList, this, (activity as? HubActivity)?.partyToSeat, (activity as? HubActivity)?.selectedCheckin, checkinActionMode, (activity as? HubActivity)?.partyActionMode)
    }

    override fun setCheckins(payload: ArrayList<EpicuriCustomer.Checkin>?) {
        unseatedListAdapter?.setCheckins(payload)
        Utils.reloadWaitingListSelection(unseatedListAdapter, unseatedList, this, (activity as? HubActivity)?.partyToSeat, (activity as? HubActivity)?.selectedCheckin, checkinActionMode, (activity as? HubActivity)?.partyActionMode)
    }

    override fun getCheckedItemPosition(): Int{
        return unseatedList?.checkedItemPosition?:-1
    }

    override fun setItemChecked(checkedItem: Int, b: Boolean) {
        unseatedList?.setItemChecked(checkedItem, b)
    }

    override fun finishActionMode() {
        checkinActionMode?.finish()
        (activity as? HubActivity)?.partyActionMode?.finish()
    }
}
