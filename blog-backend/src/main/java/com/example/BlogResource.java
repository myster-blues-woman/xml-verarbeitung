package com.example;

import com.example.model.Blog;
import com.example.service.BlogService;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/blog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Blog API", description = "API von Blogs")
@Transactional
public class BlogResource {

    @Inject
    BlogService blogService;

    @GET
    @Operation(summary = "Alle Blogs abrufen", description = "Gibt die Liste aller gespeicherten Blogs zurück")
    public List<Blog> listAll() {
        return Blog.listAll();
    }

    @POST
    @Operation(summary = "Neuer Blog speichern", description = "Speichert ein Blog in der Datenbank")
    public Blog create(Blog blog) {
        blogService.saveBlogAndSendValidation(blog);
        return blog;
    }

    @GET
    @Path("validated")
    @Operation(summary = "Validierte Blogs abrufen", description = "Gibt eine Liste aller validierten Blogs zurück")
    public List<Blog> listValidated() {
        return Blog.list("validated", true);
    }
}
