package org.knowm.xchange.latoken.service;

import java.util.Base64;

import javax.crypto.Mac;

import org.knowm.xchange.service.BaseParamsDigest;

import si.mazi.rescu.RestInvocation;

public class LatokenHmacDigest extends BaseParamsDigest {

	private LatokenHmacDigest(String secretKeyBase64) {
		super(secretKeyBase64, HMAC_SHA_256);
	}

	public static LatokenHmacDigest createInstance(String secretKeyBase64) {
		return secretKeyBase64 == null ? null : new LatokenHmacDigest(secretKeyBase64);
	}

	@Override
	public String digestParams(RestInvocation restInvocation) {
		
		String path = restInvocation.getPath();
		String queryParameters = restInvocation.getQueryString();
		String toSign = path + "?" + queryParameters;
	    
		Mac mac = getMac();
		mac.update(toSign.getBytes());
		return Base64.getEncoder().encodeToString(mac.doFinal());
	}
}
