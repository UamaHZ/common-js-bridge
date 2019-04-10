package com.uama.webview

import android.Manifest
import android.app.Application
import android.provider.ContactsContract
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.PermissionUtils

class H5RouteUtils {
    companion object {
        //获取当前网络状态
        fun _app_getNetstatus() =
                NetStatus(when (NetworkUtils.getNetworkType()) {
                    NetworkUtils.NetworkType.NETWORK_NO,
                    NetworkUtils.NetworkType.NETWORK_UNKNOWN -> 0
                    NetworkUtils.NetworkType.NETWORK_WIFI -> 2
                    else -> 1
                })

        //获取设备通信录
        fun _app_getPhonebook(context: Application): MutableList<PhoneBook> {
            var phoneBookList = mutableListOf<PhoneBook>()
            if(!PermissionUtils.isGranted(Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS)){
                PermissionUtils.permission(PermissionConstants.CONTACTS).callback(object: PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        phoneBookList = getPhoneBook(context)
                    }

                    override fun onDenied() {

                    }
                }).request()
            }else{
                phoneBookList = getPhoneBook(context)
            }

            return phoneBookList
        }

        fun getPhoneBook(context: Application): MutableList<PhoneBook>{
            val phoneBookList = mutableListOf<PhoneBook>()
            //联系人的Uri，也就是content://com.android.contacts/contacts
            val uri = ContactsContract.Contacts.CONTENT_URI
            //指定获取_id和display_name两列数据，display_name即为姓名
            val projection = arrayOf(ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME)
            //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(0)
                    //获取姓名
                    val name = cursor.getString(1)
                    //指定获取NUMBER这一列数据
                    val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phoneBook = PhoneBook(name, mutableListOf())
                    //根据联系人的ID获取此人的电话号码
                    val phonesCusor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                            null,
                            null);

                    //因为每个联系人可能有多个电话号码，所以需要遍历
                    if (phonesCusor != null && phonesCusor.moveToFirst()) {
                        do {
                            phoneBook.phoneList?.add(phonesCusor.getString(0))
                        } while (phonesCusor.moveToNext())
                    }
                    phoneBookList.add(phoneBook)
                    phonesCusor?.close()
                } while (cursor.moveToNext())
            }
            cursor?.close()
            return phoneBookList
        }
    }

}
