package org.example.url_shortener

import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
class UrlShortenerResource(private val urlShortenerService: UrlShortenerService) {

    @GET
    fun helloWorld(): Uni<String> = Uni.createFrom().item("hello world")

    @GET
    @Path("/{url}")
    fun redirectToUrl(@PathParam("url") url: String): Uni<RestResponse<URI>>? {
        return urlShortenerService.getUrlItem(url)
            .onItem()
            .transform { urlItem -> if (urlItem != null) RestResponse.seeOther(URI.create(urlItem.url)) else RestResponse.notFound() }
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    fun createUrlShortener(url: String) = urlShortenerService.createShortenedUrl(url)
}