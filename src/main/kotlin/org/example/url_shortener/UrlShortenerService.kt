package org.example.url_shortener

import io.quarkus.runtime.StartupEvent
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.tuples.Tuple2
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.RowSet
import io.vertx.mutiny.sqlclient.Tuple
import org.eclipse.microprofile.opentracing.Traced
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

@Traced
@ApplicationScoped
class UrlShortenerService(private val pgClient: PgPool) {

    fun getUrl(alias: String): Uni<String?> {
        return pgClient.preparedQuery("SELECT url from url_shortener where alias = $1")
            .execute(Tuple.of(alias))
            .onItem().transform(RowSet<Row>::iterator)
            .onItem().transform { iterator ->
                if (iterator.hasNext()) iterator.next().getString("url") else null
            }
    }

    fun createShortenedUrl(url: String): Uni<String?>? {
        return pgClient.preparedQuery("select alias from url_shortener where url=$1").execute(Tuple.of(url))
            .flatMap { pgRowSet ->
                if (pgRowSet.iterator().hasNext()) {
                    Uni.createFrom().item(pgRowSet.iterator().next().getString("alias"))
                } else {
                    getRandomString()
                        .flatMap { alias ->
                            pgClient.preparedQuery(
                                "INSERT INTO url_shortener (url, alias) " +
                                        "values ($1,$2) RETURNING alias"
                            ).execute(Tuple.of(url, alias)).onItem().transform { r ->
                                r.iterator().next().getString("alias")
                            }
                        }
                }
            }
    }

    private fun getRandomString(): Uni<String> =
        Multi.createBy().repeating()
            .uni(
                { generateRandomString() },
                { index: String? -> checkIfIndexIsUnique(index!!) }
            )
            .until { tuple: Tuple2<String, Boolean> -> tuple.item2 }
            .map { tuple: Tuple2<String, Boolean> -> tuple.item1 }.select().first().toUni()

    private fun generateRandomString(): String = UUID.randomUUID().toString().substring(0, 8)

    private fun checkIfIndexIsUnique(index: String): Uni<Tuple2<String, Boolean>>? {
        return pgClient.preparedQuery("select id from url_shortener where alias = $1")
            .execute(Tuple.of(index))
            .flatMap { result -> Uni.createFrom().item(Tuple2.of(index, result.iterator().hasNext())) }
    }

    fun createTableOnStartup(@Observes startupEvent: StartupEvent) {
        pgClient.query(
            "CREATE TABLE IF NOT EXISTS url_shortener (id SERIAL PRIMARY KEY, url VARCHAR(264) not null, " +
                    "alias VARCHAR(20) not null)"
        )
            .execute()
            .await().indefinitely()
    }
}