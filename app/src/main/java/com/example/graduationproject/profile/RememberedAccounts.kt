package com.example.graduationproject.profile

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class RememberedAccount(val id:Int,val name:String,val token:String)
internal class RememberedAccounts(context:Context) {
    private val prefs=context.getSharedPreferences("RememberedAccounts",Context.MODE_PRIVATE)
    private val alias="eldercare_saved_accounts_v1"
    private fun key():SecretKey {
        val store=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias,null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
        }.generateKey()
    }
    fun all():List<RememberedAccount> = runCatching {
        val value=prefs.getString("encrypted",null) ?: return emptyList()
        val data=Base64.decode(value,Base64.NO_WRAP)
        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,data.copyOfRange(0,12)))
        val array=JSONArray(String(cipher.doFinal(data.copyOfRange(12,data.size)),Charsets.UTF_8))
        (0 until array.length()).map { i ->
            val o=array.getJSONObject(i)
            RememberedAccount(o.getInt("id"),o.getString("name"),o.getString("token"))
        }
    }.getOrDefault(emptyList())
    private fun write(list:List<RememberedAccount>) {
        val array=JSONArray()
        list.forEach { array.put(JSONObject().put("id",it.id).put("name",it.name).put("token",it.token)) }
        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE,key())
        val encrypted=cipher.iv+cipher.doFinal(array.toString().toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString("encrypted",Base64.encodeToString(encrypted,Base64.NO_WRAP)).commit())
    }
    fun save(account:RememberedAccount) = write(all().filterNot { it.id==account.id }+account)
    fun remove(id:Int) = write(all().filterNot { it.id==id })
    fun rename(id:Int,name:String) = write(all().map { if (it.id==id) it.copy(name=name) else it })
}
