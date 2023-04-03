package org.example.url_shortener

import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/")
class UrlShortenerResource(private val urlShortenerService: UrlShortenerService) {

    @GET
    fun helloWorld(): Uni<String> = Uni.createFrom().item("hello world")

    @GET
    @Path("/{url}")
    fun redirectToUrl(@PathParam("url") url: String): Uni<RestResponse<URI>>? {
        return urlShortenerService.getUrlItem(url)
            .onItem()
            .transform { urlItem ->
                if (urlItem != null) {
                    RestResponse.seeOther(URI.create(urlItem.url))
                } else {
                    RestResponse.notFound()
                }
            }
    }

    @POST
    @Path("/create")
    fun createUrlShortener(request: CreateUrlShortenerRequest): Uni<String?>? {
        if(request.url == null) throw IllegalArgumentException("url must be specified")

        return urlShortenerService.createShortenedUrl(request.url!!)
    }
}

data class CreateUrlShortenerRequest(var url: String? = null)
