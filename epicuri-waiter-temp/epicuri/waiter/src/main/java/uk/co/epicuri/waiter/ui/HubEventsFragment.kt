package uk.co.epicuri.waiter.ui

import android.os.Bundle

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import butterknife.ButterKnife

import butterknife.InjectView
import kotlinx.android.synthetic.main.fragment_hub_events.*
import kotlinx.android.synthetic.main.fragment_hub_events.view.*
import uk.co.epicuri.waiter.R
import uk.co.epicuri.waiter.adapters.EventAdapter
import uk.co.epicuri.waiter.interfaces.HubListener
import uk.co.epicuri.waiter.interfaces.OnEventsChangedListener
import uk.co.epicuri.waiter.model.EpicuriEvent
import uk.co.epicuri.waiter.model.EpicuriSessionDetail
class HubEventsFragment : Fragment(), AdapterView.OnItemClickListener, OnEventsChangedListener {
    private var hub: HubListener? = null
    private var eventAdapter: EventAdapter? = null
    private var eventActionMode: ActionMode? = null
    private var eventSelected: EpicuriEvent.HubNotification? = null

    companion object {
        @JvmStatic
        fun newInstance(listener: HubListener): HubEventsFragment {
            val fragment = HubEventsFragment()
            fragment.hub = listener
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hub_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventAdapter = EventAdapter(context)
        eventList.adapter = eventAdapter
        eventList.onItemClickListener = this
        eventList.choiceMode = ListView.CHOICE_MODE_SINGLE
        eventList.emptyView = view.findViewById(R.id.eventListEmpty)
    }

    override fun onResume() {
        super.onResume()
        hub?.refresh()
    }

    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        val newEventSelected = eventAdapter?.getItem(i) as EpicuriEvent.HubNotification

        if (newEventSelected == eventSelected) {
            hub?.acknowledge(eventSelected)
            eventActionMode?.finish()
            return
        }

        eventSelected = newEventSelected
        hub?.highlightTablesForSession(eventSelected?.sessionId)
        if (eventActionMode == null) {
            eventActionMode = (activity as AppCompatActivity).startSupportActionMode(EventActionMode())
            eventActionMode?.invalidate()
        } else {
            eventActionMode?.invalidate()
        }
    }

    override fun onEventsChangedListener(sessions: List<EpicuriSessionDetail>?) {
        eventAdapter?.setSessionState(sessions)
        eventAdapter?.notifyDataSetChanged()
    }

    override fun onEventsAddedListener(notifications: List<EpicuriEvent.Notification>?) {
        eventAdapter?.setState(notifications)
        eventAdapter?.notifyDataSetChanged()
    }

    inner class EventActionMode : ActionMode.Callback {

        var postponeItem: MenuItem? = null

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            activity?.menuInflater?.inflate(R.menu.action_eventhandler, menu)
            postponeItem = menu.findItem(R.id.menu_postpone)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            postponeItem?.isVisible = eventSelected?.type == EpicuriEvent.Type.TYPE_SCHEDULED
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_acknowledge -> {
                    hub?.acknowledge(eventSelected)
                    eventActionMode?.finish()
                    return true
                }
                R.id.menu_postpone -> {
                    hub?.postpone(eventSelected)
                    eventActionMode?.finish()
                    return true
                }
                R.id.menu_viewSession -> {
                    hub?.launchSession(eventSelected?.sessionId)
                    eventActionMode?.finish()
                    return true
                }
                else -> false
            }
        }
        override fun onDestroyActionMode(mode: ActionMode) {
            eventList.clearChoices()
            eventList.requestLayout()
            eventActionMode = null
            eventSelected = null
            hub?.highlightNoTables()
        }
    }

    override fun finishActionMode() {
        eventActionMode?.finish()
    }
}
