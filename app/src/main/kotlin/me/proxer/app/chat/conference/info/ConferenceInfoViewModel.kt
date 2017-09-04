package me.proxer.app.chat.conference.info

import android.app.Application
import me.proxer.app.MainApplication.Companion.api
import me.proxer.app.base.BaseContentViewModel
import me.proxer.library.api.Endpoint
import me.proxer.library.entity.messenger.ConferenceInfo
import kotlin.properties.Delegates

/**
 * @author Ruben Gees
 */
class ConferenceInfoViewModel(application: Application) : BaseContentViewModel<ConferenceInfo>(application) {

    override val endpoint: Endpoint<ConferenceInfo>
        get() = api.messenger().conferenceInfo(conferenceId)

    var conferenceId by Delegates.notNull<String>()
}
