package org.knowm.xchange.latoken.service;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.latoken.LatokenExchange;
import org.knowm.xchange.latoken.dto.trade.LatokenOrderSide;
import org.knowm.xchange.latoken.dto.trade.LatokenTestOrder;
import org.knowm.xchange.latoken.dto.trade.LatokenUserTrades;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatokenTradeServiceIntegration {

  static Logger LOG = LoggerFactory.getLogger(LatokenTradeServiceIntegration.class);

  static Exchange exchange;
  static LatokenTradeService tradeService;
  static Calendar startOfTheYear;

  @BeforeClass
  public static void beforeClass() {
    exchange =
        ExchangeFactory.INSTANCE.createExchange(
            LatokenExchange.class, "api-v1-XXX", "api-v1-secret-YYY");
    tradeService = (LatokenTradeService) exchange.getTradeService();
    startOfTheYear = Calendar.getInstance();
    startOfTheYear.set(2019, 0, 0, 0, 0);
  }

  @Before
  public void before() {
    Assume.assumeNotNull(exchange.getExchangeSpecification().getApiKey());
  }

  @Test
  public void openOrders() throws Exception {

    DefaultOpenOrdersParamCurrencyPair params =
        (DefaultOpenOrdersParamCurrencyPair) tradeService.createOpenOrdersParams();
    params.setCurrencyPair(CurrencyPair.ETH_BTC);
    List<LimitOrder> orders = tradeService.getOpenOrders(params).getOpenOrders();
    verifyOrders(orders);
  }

  @Test
  public void newOrder() throws Exception {

    CurrencyPair pair = CurrencyPair.ETH_BTC;
    OrderType type = OrderType.BID;
    BigDecimal amount = BigDecimal.valueOf(0.01);
    BigDecimal limitPrice = BigDecimal.valueOf(0.018881);
    LimitOrder newOrder =
        new LimitOrder.Builder(type, pair)
            .originalAmount(amount)
            .limitPrice(limitPrice)
            .timestamp(new Date(System.currentTimeMillis()))
            .build();

    // Test order
    LatokenTestOrder testOrder =
        tradeService.placeLatokenTestOrder(pair, "", LatokenOrderSide.buy, limitPrice, amount);
    System.out.println(testOrder);

    // Place order
    String newOrderId = tradeService.placeLimitOrder(newOrder);
    System.out.println(newOrderId);

    // Check open orders
    DefaultOpenOrdersParamCurrencyPair params =
        (DefaultOpenOrdersParamCurrencyPair) tradeService.createOpenOrdersParams();
    params.setCurrencyPair(pair);
    List<LimitOrder> openOrders = tradeService.getOpenOrders(params).getOpenOrders();
    verifyOrders(openOrders);

    sleepToPreventRequestLimit();
    
    // Cancel
    tradeService.cancelLatokenOrder(newOrderId);

    sleepToPreventRequestLimit();
    
    // Check open orders
    openOrders = tradeService.getOpenOrders(params).getOpenOrders();
    verifyOrders(openOrders);
    
    sleepToPreventRequestLimit();
    
    // Check trade history
    LatokenUserTrades userTrades = tradeService.getLatokenUserTrades(pair, null);
    verifyTrades(userTrades);
  }

private void sleepToPreventRequestLimit() throws InterruptedException {
	// Wait a bit 
    Thread.sleep(3000);
}

  private void verifyOrders(List<LimitOrder> openOrders) {
	System.out.println("Open Orders:\n" + openOrders);
    openOrders.forEach(order -> {
    	System.out.println("Verifying: " + order);
    	assertTrue(order.getTimestamp().after(startOfTheYear.getTime()));
    });
  }
  
  private void verifyTrades(LatokenUserTrades userTrades) {
	System.out.println("User trades" + userTrades);
	userTrades.getTrades().forEach(userTrade -> {
		System.out.println("Verifying: " + userTrade);
    	assertTrue(userTrade.getTime().after(startOfTheYear.getTime()));
    });
  }
}
