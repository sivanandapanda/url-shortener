package org.example.url_shortener

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UrlShortenerService {

    private val shortenedUrls = mutableMapOf(
        "abc" to UrlItem("1", "http://www.google.com", "abc")
    )

    fun getUrlItem(shortenedUrl: String) = shortenedUrls[shortenedUrl]?.value

}