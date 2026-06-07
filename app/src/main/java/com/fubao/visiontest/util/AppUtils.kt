package com.fubao.visiontest.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object AppUtils {
    
    /**
     * 检查网络连接状态
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    /**
     * 获取应用版本信息
     */
    fun getAppVersionInfo(context: Context): Pair<String, Int> {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return Pair(packageInfo.versionName ?: "1.0", packageInfo.versionCode)
        } catch (e: Exception) {
            return Pair("1.0", 1)
        }
    }
    
    /**
     * 格式化时间戳为可读格式
     */
    fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(date)
    }
    
    /**
     * 保存测试结果的简易方法（实际应该使用 SharedPreferences 或 Room）
     */
    fun saveTestResult(context: Context, result: String) {
        // placeholder - 实际应实现数据存储
    }
}
