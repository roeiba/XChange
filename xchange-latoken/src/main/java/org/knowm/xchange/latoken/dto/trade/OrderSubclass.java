package org.knowm.xchange.latoken.dto.trade;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderSubclass {
	Limit;

	@JsonCreator
	public static OrderSubclass parse(String s) {
		try {
			return OrderSubclass.valueOf(StringUtils.capitalize(s));
		} catch (Exception e) {
			throw new RuntimeException("Unknown OrderSubclass " + s + ".");
		}
	}
}
