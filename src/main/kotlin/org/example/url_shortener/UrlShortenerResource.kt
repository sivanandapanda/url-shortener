package org.example.url_shortener

import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/")
class UrlShortenerResource(private val urlShortenerService: UrlShortenerService) {

    @GET
    fun helloWorld(): Uni<String> = Uni.createFrom().item("hello world")

    @GET
    @Path("/{url}")
    fun redirectToUrl(@PathParam("url") url: String): Uni<RestResponse<URI>>? {
      return Uni.createFrom().item(urlShortenerService.getUrlItem(url)?.let {
          RestResponse.seeOther(URI.create(it))
      } ?: RestResponse.notFound())
    }
}