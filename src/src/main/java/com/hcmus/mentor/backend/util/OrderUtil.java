package com.hcmus.mentor.backend.util;

import com.google.common.base.Strings;
import com.hcmus.mentor.backend.controller.usecase.common.ListSortDirection;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderUtil {

    private OrderUtil() {
    }

    public static List<MutablePair<String, ListSortDirection>> parseSeparated(String orderQuery) {
        if (Strings.isNullOrEmpty(orderQuery)) {
            return Collections.emptyList();
        }
        var sortingRecordsStrings = orderQuery.split("[;,]");

        return Arrays.stream(sortingRecordsStrings).map(sortingRecord -> {
            var parts = sortingRecord.split(":");
            if (parts.length != 2) {
                return null;
            }
            var direction = parts[1].toLowerCase();
            if (direction.equals("asc")) {
                return MutablePair.of(parts[0], ListSortDirection.ASCENDING);
            }
            if (direction.equals("desc")) {
                return MutablePair.of(parts[0], ListSortDirection.DESCENDING);
            }
            return null;
        }).toList();
    }

    public static <T> JPAQuery<T> addOrderBy(JPAQuery<T> expression, List<MutablePair<String, ListSortDirection>> requests, List<MutablePair<String, Path>> targets) {
        for (var request : requests) {
            var target = targets.stream().filter(t -> t.getKey().equals(request.getKey())).findFirst().orElse(null);
            if (target == null) {
                continue;
            }

            expression.orderBy(new OrderSpecifier<>(mapOrderFromListSortDirection(request.getValue()), target.getValue()));
        }

        return expression;
    }

    private static Order mapOrderFromListSortDirection(ListSortDirection direction) {
        return direction == ListSortDirection.ASCENDING ? Order.ASC : Order.DESC;
    }
}

