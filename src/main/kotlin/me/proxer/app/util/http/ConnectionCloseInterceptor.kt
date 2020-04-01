package me.proxer.app.util.http

import android.os.Build
import me.proxer.library.util.ProxerUrls
import me.proxer.library.util.ProxerUrls.hasProxerHost
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Ruben Gees
 */
class ConnectionCloseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()

        return if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
            oldRequest.url().hasProxerHost
        ) {
            val newRequest = oldRequest.newBuilder()
                .header("Connection", "close")
                .build()

            chain.proceed(newRequest)
        } else {
            chain.proceed(oldRequest)
        }
    }
}
