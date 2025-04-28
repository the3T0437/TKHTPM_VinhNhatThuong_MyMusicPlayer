package com.musicapp.mymusicplayer.database

import android.content.Context
import android.util.Log
import com.musicapp.mymusicplayer.model.Artist
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

/**
 * Interface để xử lý callback khi thao tác với database thành công hoặc thất bại
 */
interface OnDatabaseCallBack {
    fun onSuccess(id: Long)
    fun onFailure(e: Exception)
}

/**
 * Interface để xử lý callback khi lấy dữ liệu từ database
 */
interface OnGetItemCallback {
    fun onSuccess(value: Any)
    fun onFailure(e: Exception)
}

/**
 * Class chính để tương tác với database
 * Cung cấp các phương thức để thao tác với dữ liệu
 */
class DatabaseAPI(context: Context) {
    companion object{
        val onDatabaseCallBackDoNothing = object: OnDatabaseCallBack{
            override fun onSuccess(id: Long) {
            }

            override fun onFailure(e: Exception) {
            }
        }
    }

    // Khai báo các DAO để tương tác với các bảng trong database
    private val songPlayListDAO: SongPlayListDAO
    private var songDAO: SongDAO
    private var playListDAO: PlayListDAO
    private var favoriteSongDAO: FavoriteSongDAO
    private var artistDAO: ArtistDAO
    private val job = Job()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    init {
        val myRoomDatabase = MyRoomDatabase.getDatabase(context)
        songPlayListDAO = myRoomDatabase.songPlayListDao()
        songDAO = myRoomDatabase.songDao()
        playListDAO = myRoomDatabase.playListDao()
        favoriteSongDAO = myRoomDatabase.favroiteSongDAO()
        artistDAO = myRoomDatabase.artistDAO()
    }

    /**
     * Thêm bài hát vào playlist
     * @param songID ID của bài hát
     * @param playListID ID của playlist
     * @param callback Callback để xử lý kết quả
     */
    fun themSongPlayList(songID: Long,playListID: Int, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            val count = songPlayListDAO.checkSongExists(playListID, songID)

            if (count == 0) { // Nếu chưa tồn tại thì thêm mới
                val songPlayList = SongPlayList(playListID, songID)
                val id = songPlayListDAO.insertSongPlayList(songPlayList)
                songPlayList.songOrder = id;
                songPlayListDAO.updateSongPlayList(songPlayList)
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

    /**
     * Lấy danh sách liên kết giữa bài hát và playlist
     * @param danhSachLienKet Danh sách để lưu kết quả
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Xóa liên kết giữa bài hát và playlist
     * @param songPlayList Đối tượng liên kết cần xóa
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Lấy tất cả bài hát trong một playlist
     * @param playlistId ID của playlist
     * @param songs Danh sách để lưu kết quả
     * @param callback Callback để xử lý kết quả
     */
    fun getAllSongsInPlaylist(playlistId: Int, songs: ArrayList<Song>, callback: OnDatabaseCallBack){
        coroutineScope.launch {
            try{
                val songsId = songPlayListDAO.getSongsIDPlaylist(playlistId)
                songs.clear()
                songs.addAll(getSongs(songsId))
                withContext(Dispatchers.Main){
                    callback.onSuccess(songs.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    /**
     * Đổi vị trí hai bài hát trong playlist
     * @param playlistID ID của playlist
     * @param songId1 ID của bài hát thứ nhất
     * @param songId2 ID của bài hát thứ hai
     * @param callback Callback để xử lý kết quả
     */
    fun swapOrder(playlistID: Long, songId1: Long, songId2: Long, callback: OnDatabaseCallBack){
        coroutineScope.launch {
            try{
                val songPlayList1 = songPlayListDAO.getSongPlayList(playlistID, songId1)
                val songPlayList2 = songPlayListDAO.getSongPlayList(playlistID, songId2)
                val temp = songPlayList1.songOrder
                songPlayList1.songOrder = songPlayList2.songOrder
                songPlayList2.songOrder = temp

                songPlayListDAO.updateSongPlayList(songPlayList1)
                songPlayListDAO.updateSongPlayList(songPlayList2)

                withContext(Dispatchers.Main){
                    callback.onSuccess(2)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    /**
     * Thêm bài hát mới vào database
     * @param song Đối tượng bài hát cần thêm
     * @param callback Callback để xử lý kết quả
     */
    fun insertSong(song: Song, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val id = songDAO.themSong(song)
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

    /**
     * Lấy tất cả bài hát từ database
     * @param songs Danh sách để lưu kết quả
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Lấy thông tin một bài hát theo ID
     * @param songId ID của bài hát
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Cập nhật thông tin bài hát
     * @param song Đối tượng bài hát cần cập nhật
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Thêm playlist mới vào database
     * @param playList Đối tượng playlist cần thêm
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Xóa playlist và tất cả bài hát trong playlist đó
     * @param playlistID ID của playlist cần xóa
     * @param callback Callback để xử lý kết quả
     */
    fun deletePlayList(playlistID: Long, callback: OnDatabaseCallBack){
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Xóa tất cả bài hát trong playlist trước
                val songsInPlaylist = songPlayListDAO.getSongsIDPlaylist(playlistID.toInt())
                for (songId in songsInPlaylist) {
                    songPlayListDAO.deleteSongFromPlaylist(playlistID.toInt(), songId)
                }

                // Sau đó xóa playlist
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

    /**
     * Lấy tất cả playlist từ database
     * @param playlists Danh sách để lưu kết quả
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Cập nhật thông tin playlist
     * @param playList Đối tượng playlist cần cập nhật
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Thêm bài hát vào danh sách yêu thích
     * @param favoriteSong Đối tượng bài hát yêu thích cần thêm
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Xóa bài hát khỏi danh sách yêu thích
     * @param id ID của bài hát cần xóa
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Lấy tất cả bài hát trong danh sách yêu thích
     * @param arr Danh sách để lưu kết quả
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Kiểm tra bài hát có trong danh sách yêu thích không
     * @param songId ID của bài hát cần kiểm tra
     * @return FavoriteSong nếu có, null nếu không
     */
    suspend fun getFavorite(songId: Long): FavoriteSong? {
        return withContext(Dispatchers.IO) {
            favoriteSongDAO.readFavoriteSongFromId(songId)
        }
    }

    /**
     * Tìm kiếm bài hát theo tên
     * @param title Từ khóa tìm kiếm
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Lấy danh sách bài hát theo danh sách ID
     * @param songIds Danh sách ID của các bài hát
     * @return Danh sách bài hát
     */
    fun getSongs(songIds: List<Long>): ArrayList<Song>{
        val songs = arrayListOf<Song>()
        for (songId in songIds){
            val song = songDAO.getSong(songId)
            if (song == null)
                continue;
            songs.add(song)
        }
        return songs;
    }

    /**
     * Xóa bài hát khỏi database
     * @param id ID của bài hát cần xóa
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Xóa bài hát khỏi playlist
     * @param playlistId ID của playlist
     * @param songId ID của bài hát
     * @param callback Callback để xử lý kết quả
     */
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

    /**
     * Thêm nghệ sĩ mới vào database
     * @param artist Đối tượng nghệ sĩ cần thêm
     * @param callback Callback để xử lý kết quả
     */
    fun insertArtist(artist: Artist, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val id = artistDAO.insertArtist(artist)

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

    /**
     * Lấy tất cả nghệ sĩ từ database
     * @param artists Danh sách để lưu kết quả
     * @param callback Callback để xử lý kết quả
     */
    fun getAllArtists(artists:ArrayList<Artist>, callback: OnDatabaseCallBack) : Job{
        return coroutineScope.launch {
            try {
                artists.clear()
                artists.addAll(artistDAO.getAllArtists())

                withContext(Dispatchers.Main) {
                    callback.onSuccess(artists.size.toLong())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }

    /**
     * Lấy thông tin nghệ sĩ theo ID
     * @param artistId ID của nghệ sĩ
     * @param callback Callback để xử lý kết quả
     */
    fun getArtist(artistId: Long, callback: OnGetItemCallback){
        coroutineScope.launch {
            try {
                val value = artistDAO.getArtist(artistId)

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

    /**
     * Lấy thông tin nghệ sĩ theo tên
     * @param artistId Tên của nghệ sĩ
     * @param callback Callback để xử lý kết quả
     */
    fun getArtistByName(artistId: String, callback: OnGetItemCallback){
        coroutineScope.launch {
            try {
                val value = artistDAO.getArtist(artistId)

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

    /**
     * Thêm nghệ sĩ từ thông tin bài hát
     * @param song Đối tượng bài hát chứa thông tin nghệ sĩ
     * @param callback Callback để xử lý kết quả
     */
    fun insertArtistBySong(song: Song, callback: OnDatabaseCallBack) {
        coroutineScope.launch {
            try {
                val artist = Artist(song.artist)
                val value = artistDAO.insertArtist(artist)
                song.artistId = artistDAO.getArtist(song.artist)!!.id
                songDAO.updateSong(song)

                withContext(Dispatchers.Main) {
                    callback.onSuccess(value)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    song.artistId = artistDAO.getArtist("<unknown>")!!.id

                    callback.onFailure(e)
                }
            }
        }
    }
    /**
     * Lấy danh sách bài hát trong playlist
     * @param playlistId ID của playlist
     * @return Danh sách bài hát
     */
    fun getSongsInPlaylist(playlistId: Int): List<Song> {
        return songPlayListDAO.getSongsInPlayList(playlistId)
    }
}