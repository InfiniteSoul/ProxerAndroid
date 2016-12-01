package com.proxerme.app.fragment.chat

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.*
import com.proxerme.app.R
import com.proxerme.app.activity.chat.ChatActivity
import com.proxerme.app.activity.chat.NewChatActivity
import com.proxerme.app.adapter.chat.ConferenceAdapter
import com.proxerme.app.entitiy.LocalConference
import com.proxerme.app.event.ChatSynchronizationEvent
import com.proxerme.app.fragment.framework.SingleLoadingFragment
import com.proxerme.app.manager.SectionManager.Section
import com.proxerme.app.task.CachedTask
import com.proxerme.app.task.DatabaseTask
import com.proxerme.app.task.Task
import com.proxerme.app.util.Utils
import com.proxerme.app.util.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class ConferencesFragment : SingleLoadingFragment<List<LocalConference>>() {

    companion object {
        fun newInstance(): ConferencesFragment {
            return ConferencesFragment()
        }
    }

    override val section = Section.CONFERENCES
    override val isLoginRequired = true
    override val isSwipeToRefreshEnabled = false
    override val cacheStrategy = CachedTask.CacheStrategy.EXCEPTION

    private lateinit var adapter: ConferenceAdapter

    private val list: RecyclerView by bindView(R.id.list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = ConferenceAdapter()
        adapter.callback = object : ConferenceAdapter.ConferenceAdapterCallback() {
            override fun onItemClick(item: LocalConference) {
                ChatActivity.navigateTo(activity, item)
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_conferences, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_conferences, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_chat -> NewChatActivity.navigateTo(activity)
            R.id.new_group -> NewChatActivity.navigateTo(activity, isGroup = true)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.setHasFixedSize(true)
        list.layoutManager = StaggeredGridLayoutManager(Utils.calculateSpanAmount(activity),
                StaggeredGridLayoutManager.VERTICAL)
        list.adapter = adapter
    }

    override fun onDestroyView() {
        list.layoutManager = null
        list.adapter = null

        super.onDestroyView()
    }

    override fun constructTask(): Task<List<LocalConference>> {
        return DatabaseTask({ context }, { it.getConferences() })
    }

    override fun present(data: List<LocalConference>) {
        adapter.replace(data)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSynchronization(@Suppress("UNUSED_PARAMETER") event: ChatSynchronizationEvent) {
        adapter.insert(event.newEntryMap.keys)
    }
}