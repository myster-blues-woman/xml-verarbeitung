package com.example.service;

import com.example.dto.ValidationRequest;
import com.example.dto.ValidationResponse;
import com.example.model.Blog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import java.util.Optional;

@ApplicationScoped
public class BlogService {

    private static final Logger LOG = Logger.getLogger(BlogService.class);

    @Inject
    @Channel("validation-requests")
    Emitter<ValidationRequest> validationRequestEmitter;

    @Transactional
    public Blog saveBlog(Blog blog) {
        blog.persist();
        return blog;
    }

    public void saveblogAndSendValidation(Blog blog) {
        Blog savedblog = saveBlog(blog);
        sendValidationRequestAsync(savedblog);
    }

    private void sendValidationRequestAsync(Blog blog) {
        LOG.infof("Sending validation request for blog ID: %s", blog.id);
        ValidationRequest request = new ValidationRequest(blog.id, blog.name, blog.description);
        validationRequestEmitter.send(request)
                .toCompletableFuture().join();

        LOG.info("Kafka message sent successfully: " + request);
    }

    @Incoming("validation-responses")
    @Transactional
    public void processValidationResponse(ValidationResponse response) {
        LOG.infof("Received validation response: ID=%s, Valid=%s", response.id(), response.valid());

        Optional<Blog> blogOptional = Blog.findByIdOptional(response.id());
        if (blogOptional.isEmpty()) {
            LOG.warn("blog not found");
            return;
        }

        Blog blog = blogOptional.get();
        blog.validated = response.valid();
        blog.persist();

        LOG.infof("Updated blog validation status: %s -> %s", blog.id, response.valid());
    }
}
