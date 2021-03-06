package org.knowm.xchange.bibox.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

/** @author roeiba */
public class BiboxPendingHistoryCommandBody {

  /** the currency pair */
  private String pair;

  @JsonProperty("account_type")
  private BiboxAccountType accountType;

  private int page;

  private int size;

  /** the base currency */
  @JsonProperty("coin_symbol")
  private String coinSymbol;

  /** the counter currency */
  @JsonProperty("currency_symbol")
  private String currencySymbol;

  @JsonProperty("order_side")
  private BiboxOrderSide orderSide;
  
  // hide canceled order, 0-no, 1-yes
  @JsonProperty("hide_cancel")
  private int hideCancel;

  public BiboxPendingHistoryCommandBody(int page, int size) {
    this(null, null, page, size, null, null, null);
  }

  public BiboxPendingHistoryCommandBody(String pair, int page, int size) {
    this(pair, null, page, size, null, null, null);
  }
  
  public BiboxPendingHistoryCommandBody(
	      String pair,
	      BiboxAccountType accountType,
	      int page,
	      int size,
	      String coinSymbol,
	      String currencySymbol,
	      BiboxOrderSide orderSide) {
	  // Set default of hide canceled orders to 1 - hide canceled.
	  this(pair, null, page, size, null, null, null, 1); 
  }

  public BiboxPendingHistoryCommandBody(
      String pair,
      BiboxAccountType accountType,
      int page,
      int size,
      String coinSymbol,
      String currencySymbol,
      BiboxOrderSide orderSide,
      int hideCancel) {
    super();
    this.pair = pair;
    this.accountType = accountType;
    this.page = page;
    this.size = size;
    this.coinSymbol = coinSymbol;
    this.currencySymbol = currencySymbol;
    this.orderSide = orderSide;
    this.hideCancel = hideCancel;
  }

  public String getPair() {
    return pair;
  }

  public BiboxAccountType getAccountType() {
    return accountType;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public String getCoinSymbol() {
    return coinSymbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public BiboxOrderSide getOrderSide() {
    return orderSide;
  }
  
  public int getHideCancel() {
    return hideCancel;
  }
}
