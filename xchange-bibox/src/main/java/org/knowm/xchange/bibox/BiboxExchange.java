package org.knowm.xchange.bibox;

import java.io.IOException;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bibox.service.BiboxAccountService;
import org.knowm.xchange.bibox.service.BiboxMarketDataService;
import org.knowm.xchange.bibox.service.BiboxTradeService;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.exceptions.ExchangeException;

public class BiboxExchange extends BaseExchange implements Exchange {

  private ExchangeMetaData exchangeInfo;
  @Override
  protected void initServices() {
    this.marketDataService = new BiboxMarketDataService(this);
    this.accountService = new BiboxAccountService(this);
    this.tradeService = new BiboxTradeService(this);
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri("https://api.bibox.com/");
    exchangeSpecification.setHost("bibox.com");
    exchangeSpecification.setPort(80);
    exchangeSpecification.setExchangeName("Bibox");
    exchangeSpecification.setExchangeDescription("AI ENHANCED ENCRYPTED DIGITAL ASSET EXCHANGE.");

    return exchangeSpecification;
  }

  @Override
  public void remoteInit() throws IOException, ExchangeException {
    try {
      // Update local exchange meta data from remote API call
      BiboxMarketDataService marketDataService = (BiboxMarketDataService) this.marketDataService;
      exchangeInfo = marketDataService.getMetadata();
      exchangeMetaData.getCurrencyPairs().putAll(exchangeInfo.getCurrencyPairs());
      // Currencies are not returned from API and exist only on resource file
    } catch (Exception e) {
      throw new ExchangeException("Failed to initialize: " + e.getMessage(), e);
    }
  }
}
