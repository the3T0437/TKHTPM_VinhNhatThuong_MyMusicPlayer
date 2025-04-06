package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.model.SongPlayList

@Dao
interface SongPlayListDAO {
    @Insert
    suspend fun insertSongPlayList(songPlayList: SongPlayList): Long

    @Query("SELECT * FROM ${SongPlayList.TABLE_NAME}")
    suspend fun getSongPlayLists(): List<SongPlayList>

    @Query("SELECT * FROM ${SongPlayList.TABLE_NAME} WHERE ${SongPlayList.PLAY_LIST_ID} = :playListId")
    suspend fun getSongPlayListsByPlayListId(playListId: Int): List<SongPlayList>

    @Delete
    suspend fun deleteSongPlayList(songPlayList: SongPlayList): Int

    @Query(" SELECT COUNT(*) FROM song_play_list WHERE play_list_id = :playlistId AND song_id = :songId ")
    suspend fun checkSongExists(playlistId: Int, songId: Long): Int

    @Query("SELECT * FROM song WHERE ID IN (SELECT song_id FROM song_play_list WHERE play_list_id = :playListId)")
    fun getSongsInPlayList(playListId: Int): List<Song>

    @Query("DELETE FROM song_play_list WHERE play_list_id = :playlistId AND song_id = :songId")
    fun deleteSongFromPlaylist(playlistId: Int, songId: Long): Int
}
