package org.example.url_shortener

import io.quarkus.runtime.StartupEvent
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.Tuple
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

@ApplicationScoped
class UrlShortenerService(private val pgClient: PgPool) {

    fun getUrlItem(shortenedUrl: String): Uni<UrlItem?> {
        return pgClient.preparedQuery("SELECT id, url, shortened_url from url_item where shortened_url = $1")
            .execute(Tuple.of(shortenedUrl))
            .onItem().transform(RowSet<Row>::iterator)
            .onItem().transform { iterator ->
                if (iterator.hasNext()) UrlItem.from(iterator.next()) else null
            }
    }

    fun createShortenedUrl(url: String): Uni<String?>? {
        return pgClient.preparedQuery("select shortened_url from url_item where url=$1").execute(Tuple.of(url))
            .flatMap { pgRowSet ->
                if (pgRowSet.iterator().hasNext()) {
                    Uni.createFrom().item(pgRowSet.iterator().next().getString("shortened_url"))
                } else {
                    getRandomString()
                        .flatMap {
                            pgClient.preparedQuery(
                                "INSERT INTO url_item (url, shortened_url ) " +
                                        "values ($1,$2) RETURNING shortened_url"
                            )
                                .execute(Tuple.of(url, it))
                                .onItem().transform { r ->
                                    r.iterator().next().getString("shortened_url")
                                }
                        }
                }
            }
    }

    private fun getRandomString(): Uni<String> {
        var randomString = UUID.randomUUID().toString().substring(0, 8)
//        var found = false
//        while (!found) {
//            found = pgClient.preparedQuery("select id from url_item where shortened_url = $1")
//                .execute(Tuple.of(randomString))
//                .map { result -> result.iterator().hasNext() }
//                .map { }
//
//            if (!found) {
//                randomString = UUID.randomUUID().toString().substring(0, 8)
//            }
//        }

        return Uni.createFrom().item(randomString)
    }

    fun createTableOnStartup(@Observes startupEvent: StartupEvent) {
        pgClient.query(
            "CREATE TABLE IF NOT EXISTS URL_ITEM (id SERIAL PRIMARY KEY, url VARCHAR(264) not null, " +
                    "shortened_url VARCHAR(20) not null)"
        )
            .execute()
            .await().indefinitely()
    }
}