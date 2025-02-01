package com.winet.ecommerce.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public final class PagingAndSortingUtils {

	public static final String PAGE_NUMBER = "0";
	public static final String PAGE_SIZE = "20";
	public static final String DEFAULT_ORDER_BY = "asc";

	public static final String PRODUCT_DEFAULT_SORT_BY = "productId";

	public static final String PRODUCT_DEFAULT_MIN_PRICE = "0.0";
	public static final String PRODUCT_DEFAULT_MAX_PRICE = "9999.9";

	public static Pageable getPageDetails(int page, int size, String sort, String order) {
		Sort sortByAndOrder = order.equalsIgnoreCase("desc")
				? Sort.by(sort).descending()
				: Sort.by(sort).ascending();

		return PageRequest.of(page, size, sortByAndOrder);
	}

}
