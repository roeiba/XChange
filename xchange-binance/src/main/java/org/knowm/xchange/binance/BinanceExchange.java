package org.knowm.xchange.binance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.dto.meta.BinanceCurrencyPairMetaData;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.BinanceExchangeInfo;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.Filter;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.Symbol;
import org.knowm.xchange.binance.service.BinanceAccountService;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.RestProxyFactory;
import si.mazi.rescu.SynchronizedValueFactory;

public class BinanceExchange extends BaseExchange {

  private static final Logger LOG = LoggerFactory.getLogger(BinanceExchange.class);

  private static final int DEFAULT_PRECISION = 8;

  private BinanceExchangeInfo exchangeInfo;
  private Long deltaServerTimeExpire;
  private Long deltaServerTime;

  @Override
  protected void initServices() {

    this.marketDataService = new BinanceMarketDataService(this);
    this.tradeService = new BinanceTradeService(this);
    this.accountService = new BinanceAccountService(this);
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {
    throw new UnsupportedOperationException(
        "Binance uses timestamp/recvwindow rather than a nonce");
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {

    ExchangeSpecification spec = new ExchangeSpecification(this.getClass().getCanonicalName());
    spec.setSslUri("https://api.binance.com");
    spec.setHost("www.binance.com");
    spec.setPort(80);
    spec.setExchangeName("Binance");
    spec.setExchangeDescription("Binance Exchange.");
    AuthUtils.setApiAndSecretKey(spec, "binance");
    return spec;
  }

  public BinanceExchangeInfo getExchangeInfo() {

    return exchangeInfo;
  }

  @Override
  public void remoteInit() {

    try {
      // populate currency pair keys only, exchange does not provide any other metadata for download
      Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = exchangeMetaData.getCurrencyPairs();
      Map<Currency, CurrencyMetaData> currencies = exchangeMetaData.getCurrencies();

      BinanceMarketDataService marketDataService =
          (BinanceMarketDataService) this.marketDataService;
      exchangeInfo = marketDataService.getExchangeInfo();
      Symbol[] symbols = exchangeInfo.getSymbols();

      for (Symbol symbol : symbols) {
        if (symbol.getStatus().equals("BREAK")) { // Symbols with status "BREAK" are delisted
        	LOG.debug(symbol + " is delisted.");
        	continue;
        }
        CurrencyPair pair = new CurrencyPair(symbol.getBaseAsset(), symbol.getQuoteAsset());
        int basePrecision = Integer.parseInt(symbol.getBaseAssetPrecision());
        int counterPrecision = Integer.parseInt(symbol.getQuotePrecision());
        
        addCurrencyPairMetaData(currencyPairs, symbol, pair);
    
        addCurrencyMetadata(currencies, pair.base, basePrecision);
        addCurrencyMetadata(currencies, pair.counter, counterPrecision);
        
      }
    } catch (Exception e) {
      throw new ExchangeException("Failed to initialize: " + e.getMessage(), e);
    }
  }

  
  private void addCurrencyPairMetaData(
    Map<CurrencyPair, CurrencyPairMetaData> currencyPairs, 
	Symbol symbol,
	CurrencyPair pair) {
	
    CurrencyPairMetaData existingPairMetaData = currencyPairs.get(pair);
    
    // defaults
    int priceScale = DEFAULT_PRECISION;
    // Trading fee at Binance is 0.1 %
    // When using BNB, it reduces by 25% to 0.75%
    BigDecimal tradingFee = new BigDecimal("0.00075"); 
    BigDecimal minAmount = BigDecimal.ZERO;
    BigDecimal maxAmount = BigDecimal.ZERO;
    BigDecimal minNotional = BigDecimal.ZERO;
    
	// Override defaults with static
    if (existingPairMetaData != null) {
      if (existingPairMetaData.getPriceScale() != null) {
		priceScale = existingPairMetaData.getPriceScale();
	  }
	  if (existingPairMetaData.getTradingFee() != null) {
	    tradingFee = existingPairMetaData.getTradingFee();
	  }
	  if (existingPairMetaData.getMinimumAmount() != null) {
	    minAmount = existingPairMetaData.getMinimumAmount();
	  }
	  if (existingPairMetaData.getMaximumAmount() != null) {
	    maxAmount = existingPairMetaData.getMaximumAmount();
	  }
    }
	
	// Override static data with dynamic data if exist
    /**
     * Binance Filter example: 
	 * 
	 * filters: [
		{
			filterType: "PRICE_FILTER",
			minPrice: "0.00000001",
			maxPrice: "100000.00000000",
			tickSize: "0.00000001"
		},
		{
			filterType: "LOT_SIZE",
			minQty: "1.00000000",
			maxQty: "90000000.00000000",
			stepSize: "1.00000000"
		},
		{
			filterType: "MIN_NOTIONAL",
			minNotional: "0.01000000"
		}
	  ]
	*/
    Filter[] filters = symbol.getFilters(); 

    for (Filter filter : filters) {
      switch (filter.getFilterType()) {
        case "PRICE_FILTER":
          priceScale = numberOfDecimals(filter.getTickSize());
          break;
        case "LOT_SIZE":
          // In Binance, minimum amount is also minimum step size
          // Remove all trailing zeros in order to get the real scale the amount
        	  int amountScale = numberOfDecimals(filter.getStepSize());
	      minAmount = new BigDecimal(filter.getMinQty()).stripTrailingZeros().setScale(amountScale, RoundingMode.HALF_DOWN);
	      maxAmount = new BigDecimal(filter.getMaxQty()).stripTrailingZeros().setScale(amountScale, RoundingMode.HALF_DOWN);
          break;
	  	case "MIN_NOTIONAL":
	      minNotional = new BigDecimal(filter.getMinNotional());
	      break;
	  }
    }
	
	// Assign the new value
	currencyPairs.put(pair, new BinanceCurrencyPairMetaData(
	  tradingFee,
	  minAmount,
	  maxAmount,
	  priceScale,
	  minNotional,
	  null)
	);
  }


  private void addCurrencyMetadata(Map<Currency, CurrencyMetaData> currencies, Currency currency, int precision) {
	CurrencyMetaData baseMetaData = currencies.get(currency);
	if (baseMetaData == null) {
	    currencies.put(
	    		currency, 
	    		new CurrencyMetaData(
	    			precision, 
	        		currencies.containsKey(currency) ? currencies.get(currency).getWithdrawalFee() : null
			)
		);
	}
  }
  
  private int numberOfDecimals(String value) {
    return new BigDecimal(value).stripTrailingZeros().scale();
  }

  public void clearDeltaServerTime() {

    deltaServerTime = null;
  }

  public long deltaServerTime() throws IOException {

    if (deltaServerTime == null || deltaServerTimeExpire <= System.currentTimeMillis()) {

      // Do a little warm up
      Binance binance =
          RestProxyFactory.createProxy(Binance.class, getExchangeSpecification().getSslUri());
      Date serverTime = new Date(binance.time().getServerTime().getTime());

      // Assume that we are closer to the server time when we get the repose
      Date systemTime = new Date(System.currentTimeMillis());

      // Expire every 10min
      deltaServerTimeExpire = systemTime.getTime() + TimeUnit.MINUTES.toMillis(10);
      deltaServerTime = serverTime.getTime() - systemTime.getTime();

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
      LOG.trace(
          "deltaServerTime: {} - {} => {}",
          df.format(serverTime),
          df.format(systemTime),
          deltaServerTime);
    }

    return deltaServerTime;
  }
}
