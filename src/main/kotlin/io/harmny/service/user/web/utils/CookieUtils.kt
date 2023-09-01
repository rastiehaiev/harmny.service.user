package io.harmny.service.user.web.utils

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object CookieUtils {

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        val cookies: Array<Cookie>? = request.cookies
        if (!cookies.isNullOrEmpty()) {
            for (cookie in cookies) {
                if (cookie.name.equals(name)) return cookie
            }
        }
        return null
    }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = maxAge
        response.addCookie(cookie)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies: Array<Cookie>? = request.cookies
        if (!cookies.isNullOrEmpty()) {
            for (cookie in cookies) {
                if (cookie.name.equals(name)) {
                    cookie.value = ""
                    cookie.path = "/"
                    cookie.maxAge = 0
                    response.addCookie(cookie)
                }
            }
        }
    }
}
