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
import uk.co.epicuri.waiter.EpicuriApplication

import uk.co.epicuri.waiter.R
import uk.co.epicuri.waiter.adapters.SessionAdapter
import uk.co.epicuri.waiter.interfaces.OnSessionChangedListener
import uk.co.epicuri.waiter.model.EpicuriSessionDetail
import uk.co.epicuri.waiter.utils.GlobalSettings
import kotlinx.android.synthetic.main.fragment_hub_sessions.*
import uk.co.epicuri.waiter.interfaces.HubListener
class HubSessionsFragment : Fragment(), AdapterView.OnItemClickListener, OnSessionChangedListener {
    private var sessionAdapter: SessionAdapter? = null
    private var hub: HubListener? = null
    private var sessionActionMode: ActionMode? = null
    private var sessionSelected: EpicuriSessionDetail? = null

    companion object {
        @JvmStatic
        fun newInstance(listener: HubListener): HubSessionsFragment {
            val fragment = HubSessionsFragment()
            fragment.hub = listener
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        hub?.refresh()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hub_sessions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionAdapter = SessionAdapter(context)
        sessionList.adapter = sessionAdapter
        sessionList.choiceMode = ListView.CHOICE_MODE_SINGLE
        sessionList.onItemClickListener = this
        sessionList.emptyView = sessionListEmpty
    }

    private inner class SessionActionMode : ActionMode.Callback {
        private var addItems: MenuItem? = null

        private var viewSession: MenuItem? = null

        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            activity?.menuInflater?.inflate(R.menu.action_sessionhandler, menu)
            addItems = menu.findItem(R.id.menu_addItems)
            viewSession = menu.findItem(R.id.menu_viewSession)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            addItems?.isVisible = sessionSelected?.type == EpicuriSessionDetail.SessionType.DINE && !(sessionSelected?.isPaid?:false) && !(sessionSelected?.isBillRequested?:false)

            if (sessionSelected?.type == EpicuriSessionDetail.SessionType.COLLECTION || sessionSelected?.type == EpicuriSessionDetail.SessionType.DELIVERY) {
                viewSession?.title = "View Details"
            } else {
                viewSession?.title = "View Session"
            }

            menu.findItem(R.id.menu_reseatSession).isVisible = sessionSelected?.type == EpicuriSessionDetail.SessionType.DINE || sessionSelected?.type == EpicuriSessionDetail.SessionType.TAB
            return true
        }

        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.menu_viewSession -> {
                    hub?.launchSession(sessionSelected?.id)
                    actionMode.finish()
                    return true
                }
                R.id.menu_addItems -> {
                    hub?.launchSession(sessionSelected?.id, true)
                    actionMode.finish()
                    return true
                }
                R.id.menu_reseatSession -> {
                    hub?.showReseatUi(sessionSelected)
                    actionMode.finish()
                    return true
                }
            }
            return false
        }
        override fun onDestroyActionMode(actionMode: ActionMode) {
            sessionList.clearChoices()
            sessionList.requestLayout()
            sessionActionMode = null
            sessionSelected = null
            hub?.highlightNoTables()
        }

    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val newSessionSelected = sessionAdapter?.getItem(position)
        selectSession(newSessionSelected)
    }

    override fun showSession(session: EpicuriSessionDetail?) {
        sessionAdapter?.showSession(session)
    }

    override fun addSessions(sessionsWithoutTabs: List<EpicuriSessionDetail>) {
        sessionAdapter?.setState(sessionsWithoutTabs)
    }

    override fun setItemChecked(currentItem: Int, b: Boolean) = sessionList.setItemChecked(currentItem, b)

    override fun getCheckedItemPosition() = sessionList.checkedItemPosition

    private fun selectSession(sessionDetail: EpicuriSessionDetail?) {
        if (sessionDetail == sessionSelected) {
            hub?.launchSession(sessionSelected?.id)
            sessionActionMode?.finish()
            return
        }

        sessionSelected = sessionDetail
        hub?.highlightTablesForSession(sessionSelected?.id)
        if (null == sessionActionMode) {
            sessionActionMode = (activity as AppCompatActivity).startSupportActionMode(SessionActionMode())
            sessionActionMode?.invalidate()
        } else {
            sessionActionMode!!.invalidate()
        }
    }

    override fun finishActionMode() {
        sessionActionMode?.finish()
    }
}
