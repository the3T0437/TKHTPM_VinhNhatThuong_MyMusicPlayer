package com.musicapp.mymusicplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.model.SongPlayList

@Dao
interface SongPlayListDAO {
    @Insert
    fun insertSongPlayList(songPlayList: SongPlayList): Long

    @Query("SELECT * FROM ${SongPlayList.TABLE_NAME}")
    fun getSongPlayLists(): List<SongPlayList>

    @Query("SELECT * FROM ${SongPlayList.TABLE_NAME} WHERE ${SongPlayList.PLAY_LIST_ID} = :playlistId AND ${SongPlayList.SONG_ID} = :songId")
    fun getSongPlayList(playlistId: Long, songId: Long): SongPlayList

    @Query("SELECT * FROM ${SongPlayList.TABLE_NAME} WHERE ${SongPlayList.PLAY_LIST_ID} = :playListId")
    fun getSongPlayListsByPlayListId(playListId: Int): List<SongPlayList>

    @Delete
    fun deleteSongPlayList(songPlayList: SongPlayList): Int

    @Update
    fun updateSongPlayList(songPlayList: SongPlayList): Int


    @Query(" SELECT COUNT(*) FROM song_play_list WHERE play_list_id = :playlistId AND song_id = :songId ")
    fun checkSongExists(playlistId: Int, songId: Long): Int

    @Query("SELECT * FROM song WHERE ID IN (SELECT song_id FROM song_play_list WHERE play_list_id = :playListId)")
    fun getSongsInPlayList(playListId: Int): List<Song>

    @Query("SELECT ${SongPlayList.SONG_ID} FROM ${SongPlayList.TABLE_NAME} WHERE ${SongPlayList.PLAY_LIST_ID} = :playListId ORDER BY ${SongPlayList.SONG_ORDER} ASC")
    fun getSongsIDPlaylist(playListId: Int): List<Long>

    @Query("DELETE FROM song_play_list WHERE play_list_id = :playlistId AND song_id = :songId")
    fun deleteSongFromPlaylist(playlistId: Int, songId: Long): Int
}
