package utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import java.util.*

class PermissionsUtils(private val context: Context) {
    fun checkPermissions(
        permissions: List<String>,
        permissionsMissing: (List<String>, Int) -> Void,
        permissionsExist: (List<String>, Int) -> Void,
        @IntegerRes code: Int
    ) {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (permissionCodes.containsKey(permissions)) {
                    val codes =
                        permissionCodes[permissions]!!
                    codes.add(code)
                } else {
                    permissionCodes[permissions] = ArrayList(listOf(code))
                }
                permissionsMissing(permissions, code)
                return
            }
        }
        permissionsExist(permissions, code)
    }

    /*
     * When a permission is missing, it's added to this list so when the same permission is
     * requested multiple times before the request is satisfied, we can assure that
     * permissionsExist will be called for each code
     */
    private val permissionCodes: MutableMap<List<String>, MutableList<Int>>
    fun getCodesForPermissions(permissions: List<String>): List<Int> {
        return if (permissionCodes.containsKey(permissions)) {
            permissionCodes[permissions]!!
        } else ArrayList()
    }

    fun isPermitted(
        permissions: List<String>,
        @IntegerRes code: Int
    ): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (permissionCodes.containsKey(permissions)) {
                    val codes =
                        permissionCodes[permissions]!!
                    codes.add(code)
                } else {
                    permissionCodes[permissions] = ArrayList(listOf(code))
                }
                return false
            }
        }
        return true
    }

    init {
        permissionCodes =
            HashMap()
    }
}
