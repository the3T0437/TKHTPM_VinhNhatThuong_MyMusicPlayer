package com.musicapp.mymusicplayer.database

import android.content.Context
import android.util.Log
import com.musicapp.mymusicplayer.model.FavoriteSong
import com.musicapp.mymusicplayer.model.PlayList
import com.musicapp.mymusicplayer.model.Song
import com.musicapp.mymusicplayer.model.SongPlayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface OnDatabaseCallBack {
    fun onSuccess(id: Long)
    fun onFailure(e: Exception)
}

interface OnGetItemCallback {
    fun onSuccess(value: Any)
    fun onFailure(e: Exception)
}

class DatabaseAPI(context: Context) {
    private val songPlayListDAO: SongPlayListDAO
    private var songDAO: SongDAO
    private var playListDAO: PlayListDAO
    private var favoriteSongDAO: FavoriteSongDAO
    private val job = Job()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    init {
        val myRoomDatabase = MyRoomDatabase.getDatabase(context)
        songPlayListDAO = myRoomDatabase.songPlayListDao()
        songDAO = myRoomDatabase.songDao()
        playListDAO = myRoomDatabase.playListDao()
        favoriteSongDAO = myRoomDatabase.favroiteSongDAO()
    }

    fun themSongPlayList(songID: Long,playListID: Int, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            val count = songPlayListDAO.checkSongExists(playListID, songID)

            if (count == 0) { // Nếu chưa tồn tại thì thêm mới
                val songPlayList = SongPlayList(playListID, songID)
                val id = songPlayListDAO.insertSongPlayList(songPlayList)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(id)
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback.onFailure(Exception("Bài hát đã tồn tại trong playlist"))
                }
            }
        }
    }

    fun docSongPlayList(danhSachLienKet: ArrayList<SongPlayList>, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val danhSachDocVe = songPlayListDAO.getSongPlayLists()
                danhSachLienKet.addAll(danhSachDocVe)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(danhSachDocVe.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun xoaSongPlayList(songPlayList: SongPlayList, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val rowsAffected = songPlayListDAO.deleteSongPlayList(songPlayList)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(rowsAffected.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun insertSong(song: Song, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val id = songDAO.themSong(song)
                /*
                if (id != -1L) {
                    song.id = id
                }
                 */
                withContext(Dispatchers.Main) {
                    callback.onSuccess(id)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun getAllSongs(songs:ArrayList<Song>, callback: OnDatabaseCallBack) : Job{
        return coroutineScope.launch {
            try {
                songs.clear()
                songs.addAll(songDAO.getAllSongs())

                withContext(Dispatchers.Main) {
                    callback.onSuccess(songs.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun getSong(songId: Long, callback: OnGetItemCallback){
        coroutineScope.launch {
            try {
                val value = songDAO.getSong(songId)

                withContext(Dispatchers.Main) {
                    callback.onSuccess(value as Any)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun capNhatSong(song: Song, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val index = songDAO.updateSong(song)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(index.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }


    fun insertPlaylist(playList: PlayList, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val id = playListDAO.insertPlayList(playList)
                if (id != -1L) {
                    playList.id = id.toInt()
                }
                withContext(Dispatchers.Main) {
                    callback.onSuccess(id)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
    fun deletePlayList(playlistID: Long, callback: OnDatabaseCallBack){
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val rowDel = playListDAO.deletePlaylist(playlistID)
                if(rowDel > 0){
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(playlistID)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback.onFailure(Exception("Không tìm thấy Playlist để xóa"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
    fun getAllPlaylists(playlists:ArrayList<PlayList>, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val databasePlaylists = playListDAO.getAllPlaylists()
                playlists.clear()
                playlists.addAll(databasePlaylists)

                withContext(Dispatchers.Main) {
                    callback.onSuccess(databasePlaylists.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
    // 3. Dinh nghia ham cap nhat lai du lieu
    fun capNhatPlayList(playList: PlayList, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val index = playListDAO.updatePlayList(playList)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(index.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun insertFavoriteSong(favoriteSong: FavoriteSong, callback: OnDatabaseCallBack){
        coroutineScope.launch {
            try{
                val index = favoriteSongDAO.insertFavoriteSong(favoriteSong)

                if (index != -1L){
                    favoriteSong.id = index.toInt()
                }

                withContext(Dispatchers.Main){
                    callback.onSuccess(index)
                }
            }
            catch(e: Exception){
                withContext(Dispatchers.Main){
                    callback.onFailure(e)
                }
            }
        }
    }

    fun deleteFavroiteSong(id: Int, callback: OnDatabaseCallBack){
        coroutineScope.launch {
            try{
                val value = favoriteSongDAO.deleteByUserId(id.toLong())

                withContext(Dispatchers.Main){
                    callback.onSuccess(value.toLong())
                }
            }
            catch(e: Exception){
                withContext(Dispatchers.Main){
                    callback.onFailure(e)
                }
            }
        }
    }

    fun getAllFavoriteSong(arr: ArrayList<Song>, callback: OnDatabaseCallBack){
        coroutineScope.launch {
            try{
                arr.clear()
                val songIds = favoriteSongDAO.readAllFavoriteSongs().map {it.songId}
                songIds.forEach{
                    songDAO.getSong(it)?.let { arr.add(it) }
                }

                withContext(Dispatchers.Main){
                    callback.onSuccess(arr.size.toLong())
                }
            }
            catch(e: Exception){
                withContext(Dispatchers.Main){
                    callback.onFailure(e)
                }
            }
        }
    }

    fun getFavorite(songId: Long, callback: OnGetItemCallback): Job{
        return coroutineScope.launch {
            try{
                val favoriteSong = favoriteSongDAO.readFavoriteSongFromId(songId)

                withContext(Dispatchers.Main){
                    callback.onSuccess(favoriteSong as Any)
                }
            }
            catch(e: Exception){
                withContext(Dispatchers.Main){
                    callback.onFailure(e)
                }
            }
        }
    }

    suspend fun getFavorite(songId: Long): FavoriteSong? = runBlocking(coroutineScope.coroutineContext){
        var favoriteSong : FavoriteSong? = null
        favoriteSong = favoriteSongDAO.readFavoriteSongFromId(songId)
        favoriteSong
    }

    suspend fun join(){
        val job = Job()
        val coroutineScope = CoroutineScope(Dispatchers.IO + job)

        coroutineScope.launch{
            print("1")
            delay(1000)
            withContext(Dispatchers.Main){
                print("2")
            }
        }
    }
    fun searchSongs(title: String, callback: OnGetItemCallback) {
        coroutineScope.launch {
            try {
                val results = songDAO.searchSongs(title)
                Log.d("Database", "Search result size: ${results.size}")
                withContext(Dispatchers.Main) {
                    callback.onSuccess(results)
                }
            } catch (e: Exception) {
                Log.e("Database", "Search error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }



    fun getSongs(songIds: ArrayList<Long>, callback: OnGetItemCallback){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val songs = arrayListOf<Song>()
                for (songId in songIds){
                    val song = songDAO.getSong(songId)
                    if (song == null)
                        continue;
                    songs.add(song)
                }

                withContext(Dispatchers.Main) {
                    callback.onSuccess(songs as Any)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    fun deleteSong(id: Long, callback: OnDatabaseCallBack){
        coroutineScope.launch {
            try{
                val value = songDAO.deleteSong(id)

                withContext(Dispatchers.Main){
                    callback.onSuccess(value.toLong())
                }
            }
            catch(e: Exception){
                withContext(Dispatchers.Main){
                    callback.onFailure(e)
                }
            }
        }
    }
    fun getSongPlayListDAO(): SongPlayListDAO {
        return songPlayListDAO
    }
    fun deleteSongFromPlaylist(playlistId: Int, songId: Long, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val value = songPlayListDAO.deleteSongFromPlaylist(playlistId, songId)

                withContext(Dispatchers.Main) {
                    callback.onSuccess(value.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
}