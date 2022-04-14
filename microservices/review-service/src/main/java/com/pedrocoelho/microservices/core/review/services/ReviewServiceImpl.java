package com.pedrocoelho.microservices.core.review.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pedrocoelho.api.core.review.Review;
import com.pedrocoelho.api.core.review.ReviewService;
import com.pedrocoelho.api.exceptions.InvalidInputException;
import com.pedrocoelho.util.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/* INFO: We create our service implementation in order to  implemente the Java interface from the api project and annotate the class with @RestController so that Spring will call the methods in this class according to the mappings specified in the Interface class. */
@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    /* INFO: To enable ServiceUtil class from the util project, we inject it into the constructor. */
    private final ServiceUtil serviceUtil;

    public ReviewServiceImpl(ServiceUtil serviceUtil) { this.serviceUtil = serviceUtil; }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        if (productId == 213) {
            LOG.debug("No reviews found for productId: {}", productId);
            return  new ArrayList<>();
        }

        List<Review> reviewList = new ArrayList<>();
        reviewList.add(new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
        reviewList.add(new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
        reviewList.add(new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));

        LOG.debug("/reviews response size: {}", reviewList.size());

        return reviewList;
        /* INFO: Since we aren't currently using a database, we simply return a hardcoded response based on the input of productId, along with the service address supplied by the ServiceUtil. */
    }
}
