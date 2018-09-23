package me.proxer.app

import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.gojuno.koptional.Optional
import com.rubengees.rxbus.RxBus
import com.squareup.moshi.Moshi
import me.proxer.app.MainApplication.Companion.USER_AGENT
import me.proxer.app.anime.AnimeViewModel
import me.proxer.app.anime.schedule.ScheduleViewModel
import me.proxer.app.auth.LoginViewModel
import me.proxer.app.auth.LogoutViewModel
import me.proxer.app.auth.ProxerLoginTokenManager
import me.proxer.app.bookmark.BookmarkViewModel
import me.proxer.app.chat.prv.LocalConference
import me.proxer.app.chat.prv.conference.ConferenceViewModel
import me.proxer.app.chat.prv.conference.info.ConferenceInfoViewModel
import me.proxer.app.chat.prv.create.CreateConferenceViewModel
import me.proxer.app.chat.prv.message.MessengerViewModel
import me.proxer.app.chat.prv.sync.MessengerDatabase
import me.proxer.app.chat.pub.message.ChatReportViewModel
import me.proxer.app.chat.pub.message.ChatViewModel
import me.proxer.app.chat.pub.room.ChatRoomViewModel
import me.proxer.app.chat.pub.room.info.ChatRoomInfoViewModel
import me.proxer.app.forum.TopicViewModel
import me.proxer.app.info.industry.IndustryInfoViewModel
import me.proxer.app.info.industry.IndustryProjectViewModel
import me.proxer.app.info.translatorgroup.TranslatorGroupInfoViewModel
import me.proxer.app.info.translatorgroup.TranslatorGroupProjectViewModel
import me.proxer.app.manga.MangaViewModel
import me.proxer.app.media.MediaInfoViewModel
import me.proxer.app.media.TagDatabase
import me.proxer.app.media.comment.CommentViewModel
import me.proxer.app.media.discussion.DiscussionViewModel
import me.proxer.app.media.episode.EpisodeViewModel
import me.proxer.app.media.list.MediaListViewModel
import me.proxer.app.media.recommendation.RecommendationViewModel
import me.proxer.app.media.relation.RelationViewModel
import me.proxer.app.news.NewsViewModel
import me.proxer.app.notification.NotificationViewModel
import me.proxer.app.profile.about.ProfileAboutViewModel
import me.proxer.app.profile.comment.ProfileCommentViewModel
import me.proxer.app.profile.history.HistoryViewModel
import me.proxer.app.profile.info.ProfileInfoViewModel
import me.proxer.app.profile.media.ProfileMediaListViewModel
import me.proxer.app.profile.topten.TopTenViewModel
import me.proxer.app.ucp.history.UcpHistoryViewModel
import me.proxer.app.ucp.media.UcpMediaListViewModel
import me.proxer.app.ucp.overview.UcpOverviewViewModel
import me.proxer.app.ucp.topten.UcpTopTenViewModel
import me.proxer.app.util.Validators
import me.proxer.app.util.data.HawkMoshiParser
import me.proxer.app.util.data.PreferenceHelper
import me.proxer.app.util.data.StorageHelper
import me.proxer.library.api.LoginTokenManager
import me.proxer.library.api.ProxerApi
import me.proxer.library.api.ProxerApi.Builder.LoggingConstraints
import me.proxer.library.api.ProxerApi.Builder.LoggingStrategy
import me.proxer.library.enums.AnimeLanguage
import me.proxer.library.enums.Category
import me.proxer.library.enums.CommentSortCriteria
import me.proxer.library.enums.Language
import me.proxer.library.enums.UserMediaListFilterType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val API_LOGGING_TAG = "API"
private const val CHAT_DATABASE_NAME = "chat.db"
private const val TAG_DATABASE_NAME = "tag.db"

private val applicationModules = module {
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
    single { androidContext().packageManager }

    single { RxBus() }

    single {
        ProxerApi.Builder(BuildConfig.PROXER_API_KEY)
            .userAgent(USER_AGENT)
            .apply { if (BuildConfig.LOG) customLogger { message -> Timber.tag(API_LOGGING_TAG).i(message) } }
            .loggingStrategy(
                when {
                    BuildConfig.DEBUG -> LoggingStrategy.ALL
                    BuildConfig.LOG -> LoggingStrategy.API
                    else -> LoggingStrategy.NONE
                }
            )
            .loggingConstraints(
                when {
                    BuildConfig.DEBUG -> LoggingConstraints.NONE
                    else -> LoggingConstraints.URL_ONLY
                }
            )
            .loggingTag(API_LOGGING_TAG)
            .loginTokenManager(get())
            .moshi(Moshi.Builder().build())
            .client(
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
            )
            .build()
    }

    single { get<ProxerApi>().moshi() }
    single { get<ProxerApi>().client() }

    single { StorageHelper(androidContext(), get()) }
    single { PreferenceHelper(get()) }
    single { Validators(get(), get()) }

    single { Room.databaseBuilder(androidContext(), MessengerDatabase::class.java, CHAT_DATABASE_NAME).build() }
    single { Room.databaseBuilder(androidContext(), TagDatabase::class.java, TAG_DATABASE_NAME).build() }
    single { get<MessengerDatabase>().dao() }
    single { get<TagDatabase>().dao() }

    single { HawkMoshiParser(Moshi.Builder().build()) }
    single { ProxerLoginTokenManager(get(), get()) } bind LoginTokenManager::class
}

private val viewModelModule = module {
    viewModel { LoginViewModel() }
    viewModel { LogoutViewModel() }

    viewModel { NewsViewModel() }
    viewModel { NotificationViewModel() }

    viewModel { CreateConferenceViewModel() }
    viewModel { (searchQuery: String) -> ConferenceViewModel(searchQuery) }
    viewModel { (conferenceId: String) -> ConferenceInfoViewModel(conferenceId) }
    viewModel { (initialConference: LocalConference) -> MessengerViewModel(initialConference) }
    viewModel { ChatRoomViewModel() }
    viewModel { ChatReportViewModel() }
    viewModel { (chatRoomId: String) -> ChatViewModel(chatRoomId) }
    viewModel { (chatRoomId: String) -> ChatRoomInfoViewModel(chatRoomId) }

    viewModel { (category: Optional<Category>) -> BookmarkViewModel(category) }

    viewModel { parameterList ->
        MediaListViewModel(
            parameterList[0], parameterList[1], parameterList[2], parameterList[3], parameterList[4], parameterList[5],
            parameterList[6], parameterList[7], parameterList[8], parameterList[9], parameterList[10]
        )
    }

    viewModel { ScheduleViewModel() }

    viewModel { UcpHistoryViewModel() }
    viewModel { UcpOverviewViewModel() }
    viewModel { UcpTopTenViewModel() }
    viewModel { (userId: Optional<String>, username: Optional<String>) -> ProfileAboutViewModel(userId, username) }
    viewModel { (userId: Optional<String>, username: Optional<String>) -> ProfileInfoViewModel(userId, username) }
    viewModel { (userId: Optional<String>, username: Optional<String>) -> TopTenViewModel(userId, username) }
    viewModel { (userId: Optional<String>, username: Optional<String>) -> HistoryViewModel(userId, username) }
    viewModel { (category: Category, filter: Optional<UserMediaListFilterType>) ->
        UcpMediaListViewModel(category, filter)
    }

    viewModel { parameterList ->
        ProfileMediaListViewModel(parameterList[0], parameterList[1], parameterList[2], parameterList[3])
    }

    viewModel { (userId: Optional<String>, username: Optional<String>, category: Optional<Category>) ->
        ProfileCommentViewModel(userId, username, category)
    }

    viewModel { (id: String, resources: Resources) -> TopicViewModel(id, resources) }

    viewModel { (entryId: String) -> MediaInfoViewModel(entryId) }
    viewModel { (entryId: String) -> EpisodeViewModel(entryId) }
    viewModel { (entryId: String, sortCriteria: CommentSortCriteria) -> CommentViewModel(entryId, sortCriteria) }
    viewModel { (entryId: String) -> RelationViewModel(entryId) }
    viewModel { (entryId: String) -> RecommendationViewModel(entryId) }
    viewModel { (entryId: String) -> DiscussionViewModel(entryId) }

    viewModel { (industryId: String) -> IndustryInfoViewModel(industryId) }
    viewModel { (industryId: String) -> IndustryProjectViewModel(industryId) }
    viewModel { (translatorGroupId: String) -> TranslatorGroupInfoViewModel(translatorGroupId) }
    viewModel { (translatorGroupId: String) -> TranslatorGroupProjectViewModel(translatorGroupId) }

    viewModel { (entryId: String, language: Language, episode: Int) -> MangaViewModel(entryId, language, episode) }
    viewModel { (entryId: String, language: AnimeLanguage, episode: Int) -> AnimeViewModel(entryId, language, episode) }
}

val modules = listOf(applicationModules, viewModelModule)
