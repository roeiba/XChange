package org.knowm.xchange.bibox.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BiboxFundsCommandBody {

  /** coin symbol */
  @JsonProperty public final String search;

  /** 
   * -2: The review fails; 
   * -1: user canceled; 
   * 0: to be reviewed; 
   * 1: The review passes (token to be listed); 
   * 2: token listing; 
   * 3: token listing completed */
//  @JsonProperty("filter_type")
//  public final int filterType;

  /** page number，start from 1 */
  @JsonProperty public final int page;

  /** how many */
  @JsonProperty public final int size;

  public BiboxFundsCommandBody(String search, int page, int size) {
    super();
    this.search = search;
//    this.filterType = filterType;
    this.page = page;
    this.size = size;
  }

  public BiboxFundsCommandBody(String coinSymbol) {
    this(coinSymbol, 1, 100);
  }
}
