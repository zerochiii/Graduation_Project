package com.example.graduationproject.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.DataClass.ProfileUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

internal object ProfileRemoteSync {
    suspend fun updateName(token:String,name:String):Boolean = runCatching {
        val response=ApiClient.apiService.updateProfile("Bearer $token",ProfileUpdateRequest(action="name",name=name))
        response.isSuccessful && response.body()?.success==true
    }.getOrDefault(false)
    suspend fun updateAvatar(token:String,source:AvatarSource,color:String):Boolean = runCatching {
        val request=when(source) {
            is AvatarSource.Preset -> ProfileUpdateRequest(action="avatar",kind="preset",avatarId=source.avatarId,avatarColor=color)
            is AvatarSource.LocalImage -> ProfileUpdateRequest(action="avatar",kind="image",avatarColor=color,imageBase64=compress(source.path))
            is AvatarSource.RemoteImage -> ProfileUpdateRequest(action="avatar",kind="image",avatarColor=color)
        }
        val response=ApiClient.apiService.updateProfile("Bearer $token",request)
        response.isSuccessful && response.body()?.success==true
    }.getOrDefault(false)
    private suspend fun compress(path:String):String = withContext(Dispatchers.IO) {
        val options=BitmapFactory.Options().apply { inJustDecodeBounds=true }
        BitmapFactory.decodeFile(path,options)
        require(options.outWidth>0 && options.outHeight>0)
        var sample=1
        while (options.outWidth/sample>512 || options.outHeight/sample>512) sample*=2
        val bitmap=BitmapFactory.decodeFile(path,BitmapFactory.Options().apply { inSampleSize=sample }) ?: error("Invalid image")
        val out=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,out)
        bitmap.recycle()
        require(out.size()<=1000000)
        Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP)
    }
}
