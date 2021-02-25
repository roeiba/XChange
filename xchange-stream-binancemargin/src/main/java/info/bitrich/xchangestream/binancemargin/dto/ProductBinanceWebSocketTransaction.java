package info.bitrich.xchangestream.binancemargin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.binancemargin.BinanceAdapters;
import org.knowm.xchange.currency.CurrencyPair;

public class ProductBinanceWebSocketTransaction extends BaseBinanceWebSocketTransaction {

  protected final CurrencyPair currencyPair;

  public ProductBinanceWebSocketTransaction(
      @JsonProperty("e") String eventType,
      @JsonProperty("E") String eventTime,
      @JsonProperty("s") String symbol) {
    super(eventType, eventTime);
    currencyPair = BinanceAdapters.adaptSymbol(symbol);
  }

  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }
}
