package org.example.url_shortener

import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/")
class UrlShortenerResource(private val urlShortenerService: UrlShortenerService) {

    @GET
    @Path("/{alias}")
    fun redirectToUrl(alias: String): Uni<RestResponse<URI>>? {
        return urlShortenerService.getUrl(alias)
            .onItem()
            .transform { url ->
                if (url != null) {
                    RestResponse.seeOther(URI.create(url))
                } else {
                    RestResponse.notFound()
                }
            }
    }

    @POST
    @Path("/create")
    fun createUrlShortener(request: CreateUrlShortenerRequest): Uni<String?>? {
        if(request.url.isNullOrEmpty()) throw IllegalArgumentException("url must be specified")

        return urlShortenerService.createShortenedUrl(request.url!!)
    }

    @ServerExceptionMapper
    fun mapIllegalArgumentException(ex: IllegalArgumentException): Uni<RestResponse<String>> =
        Uni.createFrom().item(RestResponse.status(400, ex.message ?: "bad request"))
}

data class CreateUrlShortenerRequest(var url: String? = null)
