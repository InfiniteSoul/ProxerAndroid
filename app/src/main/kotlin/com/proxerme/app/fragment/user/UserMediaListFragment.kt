package com.proxerme.app.fragment.user

import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.*
import com.proxerme.app.R
import com.proxerme.app.activity.MediaActivity
import com.proxerme.app.adapter.user.UserMediaAdapter
import com.proxerme.app.adapter.user.UserMediaAdapter.UserMediaAdapterCallback
import com.proxerme.app.fragment.framework.PagedLoadingFragment
import com.proxerme.app.fragment.user.UserMediaListFragment.UserMediaInput
import com.proxerme.app.manager.SectionManager.Section
import com.proxerme.app.task.ProxerLoadingTask
import com.proxerme.app.task.framework.ListenableTask
import com.proxerme.app.util.DeviceUtils
import com.proxerme.library.connection.user.entitiy.UserMediaListEntry
import com.proxerme.library.connection.user.request.UserMediaListRequest
import com.proxerme.library.parameters.CategoryParameter
import com.proxerme.library.parameters.UserMediaSortParameter

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class UserMediaListFragment : PagedLoadingFragment<UserMediaInput, UserMediaListEntry>() {

    companion object {

        private const val ARGUMENT_USER_ID = "user_id"
        private const val ARGUMENT_USER_NAME = "user_name"
        private const val ARGUMENT_CATEGORY = "category"
        private const val ARGUMENT_SORT_CRITERIA = "sort_criteria"

        fun newInstance(userId: String? = null, userName: String? = null,
                        @CategoryParameter.Category category: String): UserMediaListFragment {
            if (userId.isNullOrBlank() && userName.isNullOrBlank()) {
                throw IllegalArgumentException("You must provide at least one of the arguments")
            }

            return UserMediaListFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString(ARGUMENT_USER_ID, userId)
                    this.putString(ARGUMENT_USER_NAME, userName)
                    this.putString(ARGUMENT_CATEGORY, category)
                }
            }
        }
    }

    override val section = Section.USER_MEDIA_LIST
    override val itemsOnPage = 30
    override val isSwipeToRefreshEnabled = false

    private val userId: String?
        get() = arguments.getString(ARGUMENT_USER_ID)
    private val username: String?
        get() = arguments.getString(ARGUMENT_USER_NAME)
    private var category: String
        get() = arguments.getString(ARGUMENT_CATEGORY)
        set(value) = arguments.putString(ARGUMENT_CATEGORY, value)
    private var sortCriteria: String
        get() = arguments.getString(ARGUMENT_SORT_CRITERIA, UserMediaSortParameter.NAME_ASCENDING)
        set(value) = arguments.putString(ARGUMENT_SORT_CRITERIA, value)

    override lateinit var adapter: UserMediaAdapter
    override lateinit var layoutManager: StaggeredGridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = UserMediaAdapter(category)
        adapter.callback = object : UserMediaAdapterCallback() {
            override fun onItemClick(item: UserMediaListEntry) {
                MediaActivity.navigateTo(activity, item.id, item.name)
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_user_media_list, menu)

        when (sortCriteria) {
            UserMediaSortParameter.NAME_ASCENDING -> {
                menu.findItem(R.id.ascending).isChecked = true
                menu.findItem(R.id.name).isChecked = true
            }

            UserMediaSortParameter.NAME_DESCENDING -> {
                menu.findItem(R.id.descending).isChecked = true
                menu.findItem(R.id.name).isChecked = true
            }

            UserMediaSortParameter.STATE_NAME_ASCENDING -> {
                menu.findItem(R.id.ascending).isChecked = true
                menu.findItem(R.id.state).isChecked = true
            }

            UserMediaSortParameter.STATE_NAME_DESCENDING -> {
                menu.findItem(R.id.descending).isChecked = true
                menu.findItem(R.id.state).isChecked = true
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val previousCriteria = sortCriteria

        when (item.itemId) {
            R.id.ascending -> {
                if (sortCriteria == UserMediaSortParameter.NAME_DESCENDING) {
                    sortCriteria = UserMediaSortParameter.NAME_ASCENDING
                } else if (sortCriteria == UserMediaSortParameter.STATE_NAME_DESCENDING) {
                    sortCriteria = UserMediaSortParameter.STATE_NAME_ASCENDING
                }
            }

            R.id.descending -> {
                if (sortCriteria == UserMediaSortParameter.NAME_ASCENDING) {
                    sortCriteria = UserMediaSortParameter.NAME_DESCENDING
                } else if (sortCriteria == UserMediaSortParameter.STATE_NAME_ASCENDING) {
                    sortCriteria = UserMediaSortParameter.STATE_NAME_DESCENDING
                }
            }

            R.id.name -> {
                if (sortCriteria == UserMediaSortParameter.STATE_NAME_ASCENDING) {
                    sortCriteria = UserMediaSortParameter.NAME_ASCENDING
                } else if (sortCriteria == UserMediaSortParameter.STATE_NAME_DESCENDING) {
                    sortCriteria = UserMediaSortParameter.NAME_DESCENDING
                }
            }

            R.id.state -> {
                if (sortCriteria == UserMediaSortParameter.NAME_ASCENDING) {
                    sortCriteria = UserMediaSortParameter.STATE_NAME_ASCENDING
                } else if (sortCriteria == UserMediaSortParameter.NAME_DESCENDING) {
                    sortCriteria = UserMediaSortParameter.STATE_NAME_DESCENDING
                }
            }

            else -> return false
        }

        if (previousCriteria != sortCriteria) {
            reset()

            item.isChecked = true
        }

        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        layoutManager = StaggeredGridLayoutManager(DeviceUtils.calculateSpanAmount(activity) + 1,
                StaggeredGridLayoutManager.VERTICAL)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun constructTask(): ListenableTask<UserMediaInput, Array<UserMediaListEntry>> {
        return ProxerLoadingTask({
            UserMediaListRequest(it.userId, it.username, it.page)
                    .withCategory(it.category)
                    .withSortCriteria(it.sortCriteria)
                    .withLimit(it.itemsOnPage)
        })
    }

    override fun constructInput(page: Int): UserMediaInput {
        return UserMediaInput(page, userId, username, category, sortCriteria, itemsOnPage)
    }

    override fun getEmptyString(): String {
        return getString(R.string.error_no_data_user_media_list)
    }

    class UserMediaInput(page: Int, val userId: String?, val username: String?, val category: String,
                         val sortCriteria: String, val itemsOnPage: Int) : PagedInput(page)
}