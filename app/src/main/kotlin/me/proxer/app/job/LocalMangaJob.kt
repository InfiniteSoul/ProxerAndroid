package me.proxer.app.job

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import me.proxer.app.application.MainApplication.Companion.api
import me.proxer.app.application.MainApplication.Companion.mangaDb
import me.proxer.app.event.LocalMangaJobFailedEvent
import me.proxer.app.event.LocalMangaJobFinishedEvent
import me.proxer.app.helper.NotificationHelper
import me.proxer.library.enums.Language
import me.proxer.library.util.ProxerUrls
import me.proxer.library.util.ProxerUtils
import org.greenrobot.eventbus.EventBus

/**
 * @author Ruben Gees
 */
class LocalMangaJob : Job() {

    companion object {
        const val TAG = "local_manga_job"

        private const val ID_EXTRA = "id"
        private const val EPISODE_EXTRA = "episode"
        private const val LANGUAGE_EXTRA = "language"

        fun schedule(id: String, episode: Int, language: Language) {
            val extras = PersistableBundleCompat().apply {
                putString(ID_EXTRA, id)
                putInt(EPISODE_EXTRA, episode)
                putString(LANGUAGE_EXTRA, ProxerUtils.getApiEnumName(language))
            }

            JobRequest.Builder(constructTag(id, episode, language))
                    .setExtras(extras)
                    .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                    .setExecutionWindow(1L, 100L)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(true)
                    .setPersisted(true)
                    .build()
                    .schedule()
        }

        fun cancel(id: String, episode: Int, language: Language) {
            JobManager.instance().cancelAllForTag(constructTag(id, episode, language))
        }

        fun cancelAll() {
            JobManager.instance().allJobRequests.filter { it.tag.startsWith(LocalMangaJob.TAG) }
                    .map { it.tag }
                    .plus(JobManager.instance().allJobs.filter { it is LocalMangaJob }.map {
                        (it as LocalMangaJob).let { constructTag(it.id, it.episode, it.language) }
                    }).forEach { JobManager.instance().cancelAllForTag(it) }
        }

        fun isScheduledOrRunning(id: String, episode: Int, language: Language): Boolean {
            val isScheduled = JobManager.instance().allJobRequests.find {
                it.tag == constructTag(id, episode, language)
            } != null

            val isRunning = JobManager.instance().allJobs.find {
                it is LocalMangaJob && it.isJobFor(id, episode, language) && !it.isCanceled && !it.isFinished
            } != null

            return isScheduled || isRunning
        }

        fun constructTag(id: String, episode: Int, language: Language): String {
            return "${TAG}_${id}_${episode}_${ProxerUtils.getApiEnumName(language)}"
        }
    }

    val id: String
        get() = params.extras.getString(ID_EXTRA, "-1") ?: throw IllegalArgumentException("No extras passed")

    val episode: Int
        get() = params.extras.getInt(EPISODE_EXTRA, -1)

    val language: Language
        get() = ProxerUtils.toApiEnum(Language::class.java, params.extras.getString(LANGUAGE_EXTRA, "en"))
                ?: throw IllegalArgumentException("No extras passed")

    override fun onRunJob(params: Params): Result {
        try {
            if (mangaDb.findEntry(id) == null) {
                mangaDb.insertEntry(api.info().entryCore(id).build().execute())
            }

            val chapter = api.manga().chapter(id, episode, language).build().execute()

            for (it in chapter.pages) {
                if (isCanceled) {
                    EventBus.getDefault().post(LocalMangaJobFailedEvent(id, episode, language))

                    return Result.FAILURE
                }

                Glide.with(context)
                        .load(ProxerUrls.mangaPageImage(chapter.server, id, chapter.id, it.name).toString())
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get()
            }

            mangaDb.insertChapter(chapter, episode, language)

            EventBus.getDefault().post(LocalMangaJobFinishedEvent(id, episode, language))

            return Result.SUCCESS
        } catch (error: Throwable) {
            EventBus.getDefault().post(LocalMangaJobFailedEvent(id, episode, language))
            NotificationHelper.showMangaDownloadErrorNotification(context, error)

            return Result.FAILURE
        }
    }

    private fun isJobFor(id: String, episode: Int, language: Language): Boolean {
        return this.id == id && this.episode == episode && this.language == language
    }
}
