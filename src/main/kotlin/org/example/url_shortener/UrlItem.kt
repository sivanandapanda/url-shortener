package org.example.url_shortener

import io.vertx.mutiny.sqlclient.Row

data class UrlItem(val id: Long, val url: String, val shortenedUrl: String) {
    companion object {
        fun from(row: Row): UrlItem {
           return UrlItem(row.getLong("id"), row.getString("url"), row.getString("shortened_url"))
        }
    }
}