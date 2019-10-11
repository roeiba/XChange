package org.knowm.xchange.latoken.dto.trade;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LatokenOrderSide {
	Sell,
	Buy;
	
	@JsonCreator
	public static LatokenOrderSide parse(String s) {
		try {
			return LatokenOrderSide.valueOf(StringUtils.capitalize(s));
		} catch (Exception e) {
			throw new RuntimeException("Unknown LatokenOrderSide " + s + ".");
		}
	}
}