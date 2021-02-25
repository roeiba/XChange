package info.bitrich.xchangestream.binancemargin;

import info.bitrich.xchangestream.binancemargin.BinancemarginStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;

import static info.bitrich.xchangestream.binancemargin.BinancemarginStreamingExchange.USE_HIGHER_UPDATE_FREQUENCY;

import org.junit.Assert;
import org.junit.Test;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

public class BinanceIntegration {

  @Test
  public void channelCreateUrlTest() {
    BinancemarginStreamingExchange exchange =
        (BinancemarginStreamingExchange)
            StreamingExchangeFactory.INSTANCE.createExchange(BinancemarginStreamingExchange.class);
    ProductSubscription.ProductSubscriptionBuilder builder = ProductSubscription.create();
    builder.addTicker(CurrencyPair.BTC_USD).addTicker(CurrencyPair.DASH_BTC);
    String buildSubscriptionStreams = exchange.buildSubscriptionStreams(builder.build());
    Assert.assertEquals("btcusd@ticker/dashbtc@ticker", buildSubscriptionStreams);

    ProductSubscription.ProductSubscriptionBuilder builder2 = ProductSubscription.create();
    builder2
        .addTicker(CurrencyPair.BTC_USD)
        .addTicker(CurrencyPair.DASH_BTC)
        .addOrderbook(CurrencyPair.ETH_BTC);
    String buildSubscriptionStreams2 = exchange.buildSubscriptionStreams(builder2.build());
    Assert.assertEquals("btcusd@ticker/dashbtc@ticker/ethbtc@depth", buildSubscriptionStreams2);
  }

  @Test
  public void channelCreateUrlWithUpdateFrequencyTest() {
    ProductSubscription.ProductSubscriptionBuilder builder = ProductSubscription.create();
    builder
        .addTicker(CurrencyPair.BTC_USD)
        .addTicker(CurrencyPair.DASH_BTC)
        .addOrderbook(CurrencyPair.ETH_BTC);
    ExchangeSpecification spec =
        StreamingExchangeFactory.INSTANCE
            .createExchange(BinancemarginStreamingExchange.class)
            .getDefaultExchangeSpecification();
    spec.setExchangeSpecificParametersItem(USE_HIGHER_UPDATE_FREQUENCY, true);
    BinancemarginStreamingExchange exchange =
        (BinancemarginStreamingExchange) StreamingExchangeFactory.INSTANCE.createExchange(spec);
    String buildSubscriptionStreams = exchange.buildSubscriptionStreams(builder.build());
    Assert.assertEquals(
        "btcusd@ticker/dashbtc@ticker/ethbtc@depth@100ms", buildSubscriptionStreams);
  }
}
