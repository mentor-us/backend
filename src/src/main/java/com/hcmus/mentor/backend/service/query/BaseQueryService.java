package com.hcmus.mentor.backend.service.query;

import com.hcmus.mentor.backend.service.query.filter.StringFilter;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseQueryService<ENTITY> extends QueryService<ENTITY> {
    /**
     * Generates a Specification that performs a "like" query on a given metaclass field.
     *
     * @param  metaclassFunction  a function that maps the root entity to the metaclass field to query
     * @param  value              the value to match against the metaclass field
     * @return                    the generated Specification
     */
    protected Specification<ENTITY> likeUpperSpecification(
            Function<Root<ENTITY>, Expression<String>> metaclassFunction,
            final String value
    ) {
        if (StringUtils.equals(value, StringUtils.stripAccents(value))) {
            return (root, query, builder) ->
                    builder.like(
                            this.convertVietnameseToNormalText(metaclassFunction.apply(root), builder),
                            wrapLikeQuery(StringUtils.stripAccents(value))
                    );
        }
        return (root, query, builder) -> builder.like(builder.upper(metaclassFunction.apply(root)), wrapLikeQuery(value));
    }

    /**
     * Generates a Specification that performs a "like" query on a given metaclass field.
     *
     * @param  builder          the criteria builder to build the predicate
     * @param  value              the value to match against the field in DB
     * @param  pathString         use root.get or Join<A, B>.get with the field name to get this
     * @return                    the generated Predicate
     */
    protected Predicate nonAccentPredicate(CriteriaBuilder builder, String value, Path<String> pathString) {
        if (StringUtils.equals(value, StringUtils.stripAccents(value))) {
            return builder.like(this.convertVietnameseToNormalText(pathString, builder), wrapLikeQuery(StringUtils.stripAccents(value)));
        }
        return builder.like(builder.upper(pathString), wrapLikeQuery(value));
    }

    /**
     * Converts a Vietnamese text expression to its non-accented counterpart and returns the result in uppercase.
     *
     * @param  vietnameseExpression  the Vietnamese text expression to be converted
     * @param  criteriaBuilder       the CriteriaBuilder used to build the expression
     * @return                       the converted non-accented text expression in uppercase
     */
    protected Expression<String> convertVietnameseToNormalText(Expression<String> vietnameseExpression, CriteriaBuilder criteriaBuilder) {
        // Create an expression to replace Vietnamese characters with their non-accented counterparts
        Expression<String> normalTextExpression = criteriaBuilder.function("StringConstants.UNACCENT", String.class, vietnameseExpression);

        // Apply the UPPER function to convert the normal text to uppercase
        Expression<String> uppercaseExpression = criteriaBuilder.upper(normalTextExpression);

        return uppercaseExpression;
    }

//    protected Expression<String> buildFullNameExpression(
//            CriteriaBuilder criteriaBuilder,
//            Join<ENTITY, User> join,
//            List<SingularAttribute<User, String>> attributes
//    ) {
//        // Concatenate the attributes with spaces in between
//        return attributes
//                .stream()
//                .map(attribute -> join.get(attribute).as(String.class))
//                .reduce((acc, attributeExpression) ->
//                        criteriaBuilder.concat(acc, criteriaBuilder.concat(criteriaBuilder.literal(StringUtils.SPACE), attributeExpression))
//                )
//                .orElse(criteriaBuilder.literal(StringUtils.EMPTY));
//    }

//    /**
//     * Extends the given Specification by adding a condition to match any string
//     * in the specified field using a StringFilter with IN clause.
//     *
//     * @param specification The original Specification to extend.
//     * @param stringFilter  The StringFilter containing the strings to match.
//     * @param fieldName     The name of the field in the entity to apply the matching condition.
//     * @param <ENTITY>      The type of the entity being queried.
//     * @return A new Specification extended to include the string matching condition.
//     * example stringFilter: 1;3;4;5 and root.field: 6;2;3;5 will true because have 3.
//     * or 1;3;4;5;6 and 2;8;9 will false because don't match any element
//     */
//    protected Specification<ENTITY> isAnyStringMatch(Specification<ENTITY> specification, StringFilter stringFilter, String fieldName) {
//        return specification.and((root, query, builder) -> {
//            List<String> queryStrings = Optional
//                    .ofNullable(stringFilter)
//                    .map(StringFilter::getIn)
//                    .orElse(Collections.emptyList())
//                    .stream()
//                    .filter(Objects::nonNull)
//                    .map(queryString ->
//                            StringConstants.PERCENT +
//                                    StringConstants.SEMI_COLON +
//                                    queryString +
//                                    StringConstants.SEMI_COLON +
//                                    StringConstants.PERCENT
//                    )
//                    .collect(Collectors.toList());
//            if (queryStrings.isEmpty()) {
//                return builder.conjunction();
//            }
//            Path<String> dbString = root.get(fieldName);
//            Expression<String> modifiedDbString = builder.concat(
//                    builder.concat(builder.literal(StringConstants.SEMI_COLON), dbString),
//                    builder.literal(StringConstants.SEMI_COLON)
//            );
//            Predicate[] likePredicates = queryStrings
//                    .stream()
//                    .map(str -> builder.like(modifiedDbString, StringConstants.PERCENT + str + StringConstants.PERCENT))
//                    .toArray(Predicate[]::new);
//            return builder.or(builder.or(likePredicates), builder.isNull(dbString));
//        });
//    }

    protected Expression<String> buildStringExpression(Root<ENTITY> root, CriteriaBuilder builder, String attributeName) {
        return builder.lower(root.get(attributeName).as(String.class));
    }

    /**
     * Generic function to build global filter from a list of field names
     *
     * @param root The root from (root, query, builder) which will be used to get data from DB.
     * @param builder  The builder from (root, query, builder) to support some query operator.
     * @param globalFilterFields A list of field names to be included in matching global string.
     * @param global A global StringFilter.
     * @return An "or" predicate represent a query statement.
     */
    protected Predicate buildGlobalFilterPredicate(
            Root<ENTITY> root,
            CriteriaBuilder builder,
            List<String> globalFilterFields,
            StringFilter global
    ) {
        String likeGlobalString = "%" + global.getContains().toLowerCase() + "%";
        Predicate[] predicates = globalFilterFields
                .stream()
                .map(globalFilterField -> builder.like(buildStringExpression(root, builder, globalFilterField), likeGlobalString))
                .toArray(Predicate[]::new);
        return builder.or(predicates);
    }
//
//    /**
//     * Builds a predicate for filtering bookings by full names.
//     *
//     * @param criteriaBuilder    the criteria builder
//     * @param fullNameExpressions a list of expressions for the full name in different languages
//     * @param name               the full name to filter by
//     * @return the predicate for filtering bookings by full names
//     */
//    protected Predicate buildFullNamePredicate(CriteriaBuilder criteriaBuilder, List<Expression<String>> fullNameExpressions, String name) {
//        String queryUnaccented = wrapLikeQuery(StringUtils.stripAccents(name));
//        String queryAccented = wrapLikeQuery(name);
//
//        List<Expression<String>> unaccentedExpressions = fullNameExpressions
//                .stream()
//                .map(expression -> criteriaBuilder.function(StringConstants.UNACCENT, String.class, criteriaBuilder.upper(expression)))
//                .collect(Collectors.toList());
//
//        Predicate[] likePredicates;
//        if (StringUtils.equals(name, StringUtils.stripAccents(name))) {
//            likePredicates =
//                    unaccentedExpressions
//                            .stream()
//                            .map(expression -> criteriaBuilder.like(expression, queryUnaccented))
//                            .toArray(Predicate[]::new);
//        } else {
//            likePredicates =
//                    fullNameExpressions
//                            .stream()
//                            .map(expression -> criteriaBuilder.like(criteriaBuilder.upper(expression), queryAccented))
//                            .toArray(Predicate[]::new);
//        }
//
//        return criteriaBuilder.or(likePredicates);
//    }

    /**
     * Builds a Specification for filtering entities based on a StringFilter with customizable delimiters for each string.
     *
     * @param stringFilter The StringFilter containing the criteria for filtering.
     * @param fieldName    The name of the field in the entity to which the filter will be applied.
     * @param delimeter  The delimiter used to split the strings in the filter.
     * @return A Specification representing the filter criteria for the specified field with customizable delimiters.
     */
    protected Specification<ENTITY> buildSplittedStringSpecification(StringFilter stringFilter, String fieldName, String delimeter) {
        return (root, query, builder) -> {
            if (Optional.ofNullable(stringFilter.getIn()).isPresent()) {
                // Convert the 'in' values to a list of query strings
                List<String> queryStrings = stringFilter
                        .getIn()
                        .stream()
                        .map(queryString -> delimeter + queryString + delimeter)
                        .collect(Collectors.toList());

                // Access the specified field in the entity
                Path<String> dbString = root.get(fieldName);

                // Concatenate the field value with delimiters for pattern matching
                Expression<String> modifiedDbString = builder.concat(
                        builder.concat(builder.literal(delimeter), dbString),
                        builder.literal(delimeter)
                );

                // Create a list of LIKE predicates for each query string
                List<Predicate> likePredicates = queryStrings
                        .stream()
                        .map(str -> builder.like(modifiedDbString, "%" + str + "%"))
                        .collect(Collectors.toList());

                // Combine the LIKE predicates with an OR condition and check for NULL values
                return builder.or(builder.or(likePredicates.toArray(new Predicate[0])), builder.isNull(dbString));
            }
            return builder.isTrue(builder.literal(true));
        };
    }
}
