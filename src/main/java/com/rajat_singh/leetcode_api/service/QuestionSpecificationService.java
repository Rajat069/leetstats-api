package com.rajat_singh.leetcode_api.service;

import com.rajat_singh.leetcode_api.dto.QuestionSearchRequest; // Import this
import com.rajat_singh.leetcode_api.entity.QuestionEntity;
import com.rajat_singh.leetcode_api.entity.TopicTag;
import com.rajat_singh.leetcode_api.enums.questions.FilterOperator;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionSpecificationService {

    /**
     * Main method to build the complete specification from the filter criteria.
     */
    public Specification<QuestionEntity> build(QuestionSearchRequest.FilterCriteria filters, String searchKeyword) {

        Specification<QuestionEntity> spec = (root, query, builder) -> null;

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            spec = spec.and(withTitleContains(searchKeyword));
        }

        if (filters == null) {
            return spec;
        }

        // 2. Build a list of all individual filter specs
        List<Specification<QuestionEntity>> allSpecs = new ArrayList<>();

        // These calls are now correct because the helper method signatures are fixed
        if (filters.getDifficultyFilter() != null && filters.getDifficultyFilter().getDifficulties() != null) {
            allSpecs.add(withDifficulties(filters.getDifficultyFilter()));
        }
        if (filters.getTopicFilter() != null && filters.getTopicFilter().getTopicSlugs() != null) {
            allSpecs.add(withTopics(filters.getTopicFilter()));
        }
        if (filters.getAcceptanceFilter() != null) {
            allSpecs.add(withAcceptanceRate(filters.getAcceptanceFilter()));
        }
        if (filters.getFrontendIdFilter() != null) {
            allSpecs.add(withFrontendId(filters.getFrontendIdFilter()));
        }

        // 3. Combine all specs using either AND or OR
        if (allSpecs.isEmpty()) {
            return spec;
        }

        Specification<QuestionEntity> combinedFilters;
        if ("ALL".equalsIgnoreCase(filters.getFilterCombineType())) {
            combinedFilters = allSpecs.get(0);
            for (int i = 1; i < allSpecs.size(); i++) {
                combinedFilters = combinedFilters.and(allSpecs.get(i));
            }
        } else {
            combinedFilters = allSpecs.get(0);
            for (int i = 1; i < allSpecs.size(); i++) {
                combinedFilters = combinedFilters.or(allSpecs.get(i));
            }
        }

        return spec.and(combinedFilters);
    }

    // --- Private Helper Methods for each filter ---

    private Specification<QuestionEntity> withTitleContains(String keyword) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    private Specification<QuestionEntity> withDifficulties(QuestionSearchRequest.DifficultyFilter filter) {

        List<String> difficulties = filter.getDifficulties().stream()
                .map(Enum::name) // e.g., QuestionDifficulty.MEDIUM -> "MEDIUM"
                .map(String::toLowerCase) // "MEDIUM" -> "medium"
                .collect(Collectors.toList());

        return (root, query, builder) -> {
            Predicate predicate = builder.lower(root.get("difficulty")).in(difficulties);
            return (filter.getOperator() == FilterOperator.IS_NOT) ?
                    builder.not(predicate) : predicate;
        };
    }

    private Specification<QuestionEntity> withAcceptanceRate(QuestionSearchRequest.RangeFilter filter) {
        return (root, query, builder) ->
                builder.between(root.get("acRate"), filter.getRangeLeft(), filter.getRangeRight());
    }

    private Specification<QuestionEntity> withFrontendId(QuestionSearchRequest.RangeFilter filter) {
        return (root, query, builder) ->
                builder.between(root.get("id"), (int) filter.getRangeLeft(), (int) filter.getRangeRight());
    }

    /**
     * This is the most complex spec, handling JOINs and Subqueries.
     */
    private Specification<QuestionEntity> withTopics(QuestionSearchRequest.TopicFilter filter) {
        return (root, query, builder) -> {

            assert query != null;
            query.distinct(true);

            if (filter.getOperator() == FilterOperator.IS_NOT) {
                // --- "IS NOT" logic using Subquery ---
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<QuestionEntity> subRoot = subquery.correlate(root);
                Join<QuestionEntity, TopicTag> subTags = subRoot.join("topicTags");
                subquery.select(builder.literal(1))
                        .where(subTags.get("slug").in(filter.getTopicSlugs()));
                return builder.not(builder.exists(subquery));

            } else {
                return root.join("topicTags").get("slug").in(filter.getTopicSlugs());
            }
        };
    }
}