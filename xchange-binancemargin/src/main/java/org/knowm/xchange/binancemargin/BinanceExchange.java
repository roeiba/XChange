package org.knowm.xchange.binancemargin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binancemargin.dto.account.AssetDetail;
import org.knowm.xchange.binancemargin.dto.meta.exchangeinfo.BinanceExchangeInfo;
import org.knowm.xchange.binancemargin.dto.meta.exchangeinfo.Filter;
import org.knowm.xchange.binancemargin.dto.meta.exchangeinfo.Symbol;
import org.knowm.xchange.binancemargin.service.BinanceAccountService;
import org.knowm.xchange.binancemargin.service.BinanceMarketDataService;
import org.knowm.xchange.binancemargin.service.BinanceTradeService;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.mazi.rescu.SynchronizedValueFactory;

public class BinanceExchange extends BaseExchange {

  private static final Logger LOG = LoggerFactory.getLogger(BinanceExchange.class);
  private static final int DEFAULT_PRECISION = 8;
  
  private static ResilienceRegistries RESILIENCE_REGISTRIES;

  private BinanceExchangeInfo exchangeInfo;
  private BinanceAuthenticated binance;
  private SynchronizedValueFactory<Long> timestampFactory;

  @Override
  protected void initServices() {
    this.binance =
        ExchangeRestProxyBuilder.forInterface(
                BinanceAuthenticated.class, getExchangeSpecification())
            .build();
    this.timestampFactory =
        new BinanceTimestampFactory(
            binance, getExchangeSpecification().getResilience(), getResilienceRegistries());
    this.marketDataService = new BinanceMarketDataService(this, binance, getResilienceRegistries());
    this.tradeService = new BinanceTradeService(this, binance, getResilienceRegistries());
    this.accountService = new BinanceAccountService(this, binance, getResilienceRegistries());
  }

  public SynchronizedValueFactory<Long> getTimestampFactory() {
    return timestampFactory;
  }

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {
    throw new UnsupportedOperationException(
        "Binance uses timestamp/recvwindow rather than a nonce");
  }

  public static void resetResilienceRegistries() {
    RESILIENCE_REGISTRIES = null;
  }

  @Override
  public ResilienceRegistries getResilienceRegistries() {
    if (RESILIENCE_REGISTRIES == null) {
      RESILIENCE_REGISTRIES = BinanceResilience.createRegistries();
    }
    return RESILIENCE_REGISTRIES;
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {

    ExchangeSpecification spec = new ExchangeSpecification(this.getClass());
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

      BinanceAccountService accountService = (BinanceAccountService) getAccountService();
      Map<String, AssetDetail> assetDetailMap = accountService.getAssetDetails();
      // Clear all hardcoded currencies when loading dynamically from exchange.
      if (assetDetailMap != null) {
        currencies.clear();
      }
      
      for (Symbol symbol : symbols) {
	    if (symbol.getStatus().equals("BREAK")) { // Symbols with status "BREAK" are delisted
      	  LOG.debug(symbol + " is delisted.");
      	  continue;
        }
        CurrencyPair pair = new CurrencyPair(symbol.getBaseAsset(), symbol.getQuoteAsset());
        int basePrecision = Integer.parseInt(symbol.getBaseAssetPrecision());
        int counterPrecision = Integer.parseInt(symbol.getQuotePrecision());
      
        addCurrencyPairMetaData(currencyPairs, symbol, pair);
  
        addCurrencyMetadata(currencies, assetDetailMap, pair.base, basePrecision);
        addCurrencyMetadata(currencies, assetDetailMap, pair.counter, counterPrecision);
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
    int priceScale = 8;
    int amountScale = 8;
    BigDecimal stepSize = null;
    
    // Trading fee at Binance is 0.1 %
    // When using BNB, it reduces by 25% to 0.075%
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
          amountScale = numberOfDecimals(filter.getStepSize());
	      minAmount = new BigDecimal(filter.getMinQty()).stripTrailingZeros().setScale(amountScale, RoundingMode.HALF_DOWN);
	      maxAmount = new BigDecimal(filter.getMaxQty()).stripTrailingZeros().setScale(amountScale, RoundingMode.HALF_DOWN);
	      stepSize = new BigDecimal(filter.getStepSize()).stripTrailingZeros();
          break;
	  	case "MIN_NOTIONAL":
	      minNotional = new BigDecimal(filter.getMinNotional());
	      break;
	  }
    }
    
	
	// Assign the new value`
    boolean marketOrderAllowed = Arrays.asList(symbol.getOrderTypes()).contains("MARKET");
    currencyPairs.put(
    	pair,
        new CurrencyPairMetaData(
    		tradingFee,
    		minAmount,
    		maxAmount,
    		minNotional, // counter min amount
            null, // counter max amount
            amountScale, // base precision
            priceScale, // counter precision
            null, // volume scale
            null, /* TODO get fee tiers, although this is not necessary now
                  because their API returns current fee directly */
            stepSize,
            null,
            marketOrderAllowed));
  }
  
  private void addCurrencyMetadata(
	  Map<Currency, CurrencyMetaData> currencies,
	  Map<String, AssetDetail> assetDetailMap,
	  Currency currency,
	  int precision) {
	
	CurrencyMetaData baseMetaData = currencies.get(currency);
	if (baseMetaData == null) {
	    currencies.put(
	    		currency, 
	    		new CurrencyMetaData(
	    			precision, 
	    			getWithdrawalFee(currencies, currency, assetDetailMap)
			)
		);
	}
  }
  
  private BigDecimal getWithdrawalFee(
      Map<Currency, CurrencyMetaData> currencies,
      Currency currency,
      Map<String, AssetDetail> assetDetailMap) {
    if (assetDetailMap != null) {
      AssetDetail asset = assetDetailMap.get(currency.getCurrencyCode());
      return asset != null ? asset.getWithdrawFee().stripTrailingZeros() : null;
    }

    return currencies.containsKey(currency) ? currencies.get(currency).getWithdrawalFee() : null;
  }
  
  private int numberOfDecimals(String value) {

    return new BigDecimal(value).stripTrailingZeros().scale();
  }
}
