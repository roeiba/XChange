package org.knowm.xchange.binance;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binancemargin.BinancemarginExchange;

public class AbstractResilienceTest {

  @Rule public WireMockRule wireMockRule = new WireMockRule();

  public static int READ_TIMEOUT_MS = 1000;

  @Before
  public void resertResilienceRegistries() {
    BinancemarginExchange.resetResilienceRegistries();
  }

  protected BinancemarginExchange createExchangeWithRetryEnabled() {
    return createExchange(true, false);
  }

  protected BinancemarginExchange createExchangeWithRetryDisabled() {
    return createExchange(false, false);
  }

  protected BinancemarginExchange createExchangeWithRateLimiterEnabled() {
    return createExchange(false, true);
  }

  protected BinancemarginExchange createExchange(boolean retryEnabled, boolean rateLimiterEnabled) {
    BinancemarginExchange exchange =
        (BinancemarginExchange)
            ExchangeFactory.INSTANCE.createExchangeWithoutSpecification(BinancemarginExchange.class);
    ExchangeSpecification specification = exchange.getDefaultExchangeSpecification();
    specification.setHost("localhost");
    specification.setSslUri("http://localhost:" + wireMockRule.port() + "/");
    specification.setPort(wireMockRule.port());
    specification.setShouldLoadRemoteMetaData(false);
    specification.setHttpReadTimeout(READ_TIMEOUT_MS);
    specification.getResilience().setRetryEnabled(retryEnabled);
    specification.getResilience().setRateLimiterEnabled(rateLimiterEnabled);
    exchange.applySpecification(specification);
    return exchange;
  }
}
