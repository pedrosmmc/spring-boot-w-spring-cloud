package com.pedrocoelho.microservices.core.recommendation.services;

import com.pedrocoelho.api.core.recommendation.Recommendation;
import com.pedrocoelho.api.core.recommendation.RecommendationService;
import com.pedrocoelho.api.exceptions.InvalidInputException;
import com.pedrocoelho.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/* INFO: We create our service implementation in order to  implemente the Java interface from the api project and annotate the class with @RestController so that Spring will call the methods in this class according to the mappings specified in the Interface class. */
@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    /* INFO: To enable ServiceUtil class from the util project, we inject it into the constructor. */
    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(ServiceUtil serviceUtil) { this.serviceUtil = serviceUtil; }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        if (productId == 213) {
            LOG.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Recommendation> recommendationList = new ArrayList<>();
        recommendationList.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productId, 4, "Author 4", 4, "Content 5", serviceUtil.getServiceAddress()));
        recommendationList.add(new Recommendation(productId, 5, "Author 5", 5, "Content 5", serviceUtil.getServiceAddress()));

        LOG.debug("/recommendation response size: {}", recommendationList.size());

        return recommendationList;
    }
}
