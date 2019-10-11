package org.knowm.xchange.latoken.dto.exchangeinfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response schema:
 * <pre>
 *	{
 * 		"publicEndpoints": [
 *			{
 *				"endpoint": "get:/api/v1/ExchangeInfo/pairs",
 *				"timePeriod": "1s",
 *				"requestLimit": 1
 *			}
 *		],
 *		"signedEndpoints": [
 *			{
 *				"endpoint": "get:/api/v1/Account/account_info",
 *				"timePeriod": "1s",
 *				"requestLimit": 1
 *			}
 *		]
 *	}
 * </pre>
 * 
 * @author Ezer
 */
public class LatokenRateLimits {

	private final List<LatokenRateLimit> publicEndpoints;
	private final List<LatokenRateLimit> signedEndpoints;

	/**
	 * C'tor
	 * 
	 * @param publicEndpoints
	 * @param signedEndpoints
	 */
	public LatokenRateLimits(
			@JsonProperty("publicEndpoints") List<LatokenRateLimit> publicEndpoints,
			@JsonProperty("signedEndpoints") List<LatokenRateLimit> signedEndpoints) {
			
		this.publicEndpoints = publicEndpoints;
		this.signedEndpoints = signedEndpoints;
	}
	
	public List<LatokenRateLimit> getPublicEndpoints() {
	    return publicEndpoints;
	}
	
	public List<LatokenRateLimit> getSignedEndpoints() {
	    return signedEndpoints;
	}
	
	@Override
	public String toString() {
		return "LatokenRateLimits [publicEndpoints = "
	        + publicEndpoints
	        + ", signedEndpoints = "
	        + signedEndpoints
	        + "]";
	}
}
