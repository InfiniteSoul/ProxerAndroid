package me.proxer.app.chat.sync

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import me.proxer.app.chat.LocalConference
import me.proxer.app.chat.LocalMessage

/**
 * @author Ruben Gees
 */
@Dao
abstract class ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertConferences(conference: List<LocalConference>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMessages(messages: List<LocalMessage>): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertMessage(message: LocalMessage): Long

    @Query("SELECT * FROM conferences ORDER BY date DESC")
    abstract fun getConferencesLiveData(): LiveData<List<LocalConference>?>

    @Query("SELECT * FROM conferences WHERE id = :id LIMIT 1")
    abstract fun getConferenceLiveData(id: Long): LiveData<LocalConference?>

    @Query("SELECT * FROM conferences WHERE isRead = 0 ORDER BY id DESC")
    abstract fun getUnreadConferences(): List<LocalConference>

    @Query("SELECT * FROM conferences WHERE localIsRead != 0 AND isRead = 0")
    abstract fun getConferencesToMarkAsRead(): List<LocalConference>

    @Query("SELECT * FROM conferences WHERE id = :id LIMIT 1")
    abstract fun findConference(id: Long): LocalConference?

    @Query("SELECT * FROM conferences WHERE topic = :username LIMIT 1")
    abstract fun findConferenceForUser(username: String): LocalConference?

    @Query("SELECT * FROM (SELECT * FROM messages WHERE conferenceId = :conferenceId AND id < 0 ORDER BY id ASC) " +
            "UNION ALL " +
            "SELECT * FROM (SELECT * FROM messages WHERE conferenceId = :conferenceId AND id >= 0 ORDER BY id DESC)")
    abstract fun getMessagesLiveDataForConference(conferenceId: Long): LiveData<List<LocalMessage>>

    @Query("SELECT COUNT(*) FROM messages WHERE conferenceId = :conferenceId AND id = :lastReadMessageId")
    abstract fun getUnreadMessageAmountForConference(conferenceId: Long, lastReadMessageId: Long): Int

    @Query("SELECT * FROM messages WHERE conferenceId = :conferenceId AND id >= 0 ORDER BY id DESC LIMIT :amount")
    abstract fun getMostRecentMessagesForConference(conferenceId: Long, amount: Int): List<LocalMessage>

    @Query("SELECT * FROM messages WHERE conferenceId = :conferenceId AND id >= 0 ORDER BY id DESC LIMIT 1")
    abstract fun findMostRecentMessageForConference(conferenceId: Long): LocalMessage?

    @Query("SELECT * FROM messages WHERE conferenceId = :conferenceId AND id >= 0 ORDER BY id ASC LIMIT 1")
    abstract fun findOldestMessageForConference(conferenceId: Long): LocalMessage?

    @Query("SELECT MIN(id) FROM messages")
    abstract fun findLowestMessageId(): Long?

    @Query("SELECT * FROM messages WHERE id < 0 ORDER BY id DESC")
    abstract fun getMessagesToSend(): List<LocalMessage>

    @Query("DELETE FROM messages WHERE id = :messageId")
    abstract fun deleteMessageToSend(messageId: Long)

    @Query("UPDATE conferences SET localIsRead = 1 WHERE id = :conferenceId")
    abstract fun markConferenceAsRead(conferenceId: Long)

    @Query("UPDATE conferences SET isFullyLoaded = 1 WHERE id = :conferenceId")
    abstract fun markConferenceAsFullyLoaded(conferenceId: Long)

    @Query("DELETE FROM conferences")
    abstract fun clearConferences()

    @Query("DELETE FROM messages")
    abstract fun clearMessages()
}