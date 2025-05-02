package com.musicapp.mymusicplayer.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper(private val activity: AppCompatActivity) {

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 100
    }

    // Danh sách quyền cần yêu cầu
    private val requiredPermissions: Array<String>
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO, // Android 13+
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                )
            }
        }

    // Kiểm tra  quyền đã được cấp chưa
    fun hasPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // Yêu cầu quyền
    fun requestPermissions() {
        ActivityCompat.requestPermissions(activity, requiredPermissions, REQUEST_PERMISSIONS_CODE)
    }

    // Xử lý kết quả
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray, onGranted: () -> Unit, onDenied: () -> Unit) {
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onGranted() // Tất cả quyền đã được cấp
            } else {
                onDenied() // Quyền bị từ chối
            }
        }
    }
}