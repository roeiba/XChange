package org.knowm.xchange.latoken;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderStatus;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.latoken.dto.LatokenException;
import org.knowm.xchange.latoken.dto.account.LatokenBalance;
import org.knowm.xchange.latoken.dto.exchangeinfo.LatokenCurrency;
import org.knowm.xchange.latoken.dto.exchangeinfo.LatokenPair;
import org.knowm.xchange.latoken.dto.marketdata.LatokenOrderbook;
import org.knowm.xchange.latoken.dto.marketdata.LatokenTicker;
import org.knowm.xchange.latoken.dto.marketdata.LatokenTrade;
import org.knowm.xchange.latoken.dto.marketdata.LatokenTrades;
import org.knowm.xchange.latoken.dto.trade.LatokenOrder;
import org.knowm.xchange.latoken.dto.trade.LatokenOrderSide;
import org.knowm.xchange.latoken.dto.trade.LatokenOrderStatus;
import org.knowm.xchange.latoken.dto.trade.LatokenUserTrade;
import org.knowm.xchange.latoken.dto.trade.LatokenUserTrades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.mazi.rescu.RestProxyFactory;

public class LatokenAdapters {

	private static final Logger LOG = LoggerFactory.getLogger(LatokenAdapters.class.getName());

	protected static final Latoken latokenPublic = RestProxyFactory.createProxy(LatokenAuthenticated.class, LatokenExchange.sslUri);
	protected static final List<LatokenPair> allPairs;
	protected static final List<LatokenCurrency> allCurrencies;

	static {
		try {
			allPairs = latokenPublic.getAllPairs();
			allCurrencies = latokenPublic.getAllCurrencies();
			
		} catch (LatokenException | IOException e) {
			LOG.error("Could not retrieve currency-pairs from Latoken exchange", e);
			throw new RuntimeException();
		}
	}
	
	public static Set<Currency> getAllCurrencies() {
		return allCurrencies.stream()
			.map(latokenCurrency -> adaptCurrency(latokenCurrency))
			.collect(Collectors.toSet());
	}
	
	public static Set<CurrencyPair> getAllCurrencyPairs() {
		return allPairs.stream()
			.map(latokenPair -> adaptCurrencyPair(latokenPair))
			.collect(Collectors.toSet());
	}
	
	public static Currency adaptCurrency(LatokenCurrency latokenCurrency) {
		return Currency.getInstance(latokenCurrency.getSymbol());
	}
	
	public static CurrencyPair adaptCurrencyPair(LatokenPair latokenPair) {
		Currency base = Currency.getInstance(latokenPair.getBaseCurrency());
		Currency counter = Currency.getInstance(latokenPair.getCounterCurrency());
		
	    return new CurrencyPair(base, counter);
	}
	
	public static CurrencyPair adaptCurrencyPair(String latokenSymbol) {
		Optional<LatokenPair> oPair = allPairs.stream().filter(pair -> pair.getSymbol().equals(latokenSymbol)).findAny();
		return oPair.isPresent() ? adaptCurrencyPair(oPair.get()) : null;
	}
	
	public static Balance adaptBalance(LatokenBalance latokenBalance) {
		 return new Balance(
				 Currency.getInstance(latokenBalance.getSymbol()),
				 BigDecimal.valueOf(latokenBalance.getAmount()),
				 BigDecimal.valueOf(latokenBalance.getAvailable()),
				 BigDecimal.valueOf(latokenBalance.getFrozen()),
				 BigDecimal.ZERO, // Borrowed
				 BigDecimal.ZERO, // Loaned
				 BigDecimal.ZERO, // Withdrawing
				 BigDecimal.valueOf(latokenBalance.getPending()) // Depositing
				 ); 
	}
	
	public static Ticker adaptTicker(LatokenTicker latokenTicker) {
		return new Ticker.Builder()
				.open(BigDecimal.valueOf(latokenTicker.getOpen()))
				.last(BigDecimal.valueOf(latokenTicker.getClose()))
				.low(BigDecimal.valueOf(latokenTicker.getLow()))
				.high(BigDecimal.valueOf(latokenTicker.getHigh()))
				.volume(BigDecimal.valueOf(latokenTicker.getVolume()))
				.build();
	}
	
	public static OrderBook adaptOrderBook(LatokenOrderbook latokenOrderbook) {
		CurrencyPair pair = adaptCurrencyPair(latokenOrderbook.getSymbol());
		List<LimitOrder> asks = latokenOrderbook.getAsks().stream()
				.map(level -> new LimitOrder(OrderType.ASK, BigDecimal.valueOf(level.getAmount()), pair, null, null, BigDecimal.valueOf(level.getPrice())))
				.collect(Collectors.toList());
		List<LimitOrder> bids = latokenOrderbook.getBids().stream()
				.map(level -> new LimitOrder(OrderType.BID, BigDecimal.valueOf(level.getAmount()), pair, null, null, BigDecimal.valueOf(level.getPrice())))
				.collect(Collectors.toList());
	    
		return new OrderBook(null, asks, bids);
	}

	public static Trades adaptTrades(LatokenTrades latokenTrades) {
		CurrencyPair pair = adaptCurrencyPair(latokenTrades.getSymbol());
		List<Trade> trades = latokenTrades.getTrades().stream()
			.map(latokenTrade -> adaptTrade(latokenTrade, pair))
			.collect(Collectors.toList());
		
		return new Trades(trades, TradeSortType.SortByTimestamp);
	}
	
	public static Trade adaptTrade(LatokenTrade latokenTrade, CurrencyPair pair) {
		return new Trade(
				adaptOrderType(latokenTrade.getSide()),
				BigDecimal.valueOf(latokenTrade.getAmount()),
				pair,
				BigDecimal.valueOf(latokenTrade.getPrice()),
				latokenTrade.getTimestamp(),
				null);
	}
	
	public static LimitOrder adaptOrder(LatokenOrder order) {
		OrderType type = adaptOrderType(order.getSide());
		CurrencyPair currencyPair = adaptCurrencyPair(order.getSymbol());
		OrderStatus orderStatus = adaptOrderStatus(order.getOrderStatus());
		
		return new LimitOrder(
              type,
              BigDecimal.valueOf(order.getAmount()),
              currencyPair,
              order.getOrderId(),
              order.getTimeCreated(),
              BigDecimal.valueOf(order.getPrice()),
              null, // averagePrice - null since filled-price is unknown
              BigDecimal.valueOf(order.getExecutedAmount()),
              BigDecimal.ZERO,
              orderStatus);
	}
	
	public static OpenOrders adaptOpenOrders(List<LatokenOrder> latokenOpenOrders) {
		List<LimitOrder> openOrders = latokenOpenOrders.stream()
    			.map(latokenOrder -> LatokenAdapters.adaptOrder(latokenOrder))
    			.collect(Collectors.toList());
    		
    	return new OpenOrders(openOrders);
	}
	
	public static OrderType adaptOrderType(LatokenOrderSide side) {
		switch (side) {
		case Buy:
			return OrderType.BID;
		case Sell:
			return OrderType.ASK;
		default:
			throw new RuntimeException("Not supported order side: " + side);
		}
	}
	
	public static OrderStatus adaptOrderStatus(LatokenOrderStatus latokenOrderStatus) {
		switch (latokenOrderStatus) {
		case Active:
			return OrderStatus.NEW; // Not exactly accurate as Active includes both New and partiallyFilled orders. 
		case PartiallyFilled:
			return OrderStatus.PARTIALLY_FILLED;
		case Filled:
			return OrderStatus.FILLED;
		case Cancelled:
			return OrderStatus.CANCELED;
		
		default:
			return OrderStatus.UNKNOWN;
		}
	}
	
	public static UserTrades adaptUserTrades(LatokenUserTrades latokenUserTrades) {
		
		CurrencyPair pair = adaptCurrencyPair(latokenUserTrades.getSymbol());
		List<UserTrade> trades = latokenUserTrades.getTrades().stream()
			.map(latokenUserTrade -> adaptUserTrade(latokenUserTrade, pair))
			.collect(Collectors.toList());
		
		return new UserTrades(trades, Trades.TradeSortType.SortByTimestamp);
	}
	
	public static UserTrade adaptUserTrade(LatokenUserTrade latokenUserTrade, CurrencyPair pair) {
		return new UserTrade(
				adaptOrderType(latokenUserTrade.getSide()),
				BigDecimal.valueOf(latokenUserTrade.getAmount()),
				pair,
				BigDecimal.valueOf(latokenUserTrade.getPrice()),
				latokenUserTrade.getTime(),
				latokenUserTrade.getId(),
				latokenUserTrade.getOrderId(),
				BigDecimal.valueOf(latokenUserTrade.getFee()),
				pair.counter); // Fee is always in counter currency
	}
	
	// --------------- Convert to Latoken convention --------------------------
	
	public static String toSymbol(CurrencyPair pair) {
		return pair.base.getCurrencyCode() + pair.counter.getCurrencyCode();
	}

	public static String toSymbol(Currency currency) {
		return currency.getSymbol();
	}

	public static LatokenOrderSide toOrderSide(OrderType type) {
		switch (type) {
		case ASK:
			return LatokenOrderSide.Sell;
		case BID:
			return LatokenOrderSide.Buy;
		default:
			throw new RuntimeException("Not supported order type: " + type);
		}
	}
	
	public static LatokenOrderStatus toLatokenOrderStatus(OrderStatus status) {
		switch (status) {
		case PENDING_NEW:
		case NEW:
		case PENDING_CANCEL:
		case PENDING_REPLACE:
			return LatokenOrderStatus.Active;
		case PARTIALLY_FILLED:
			return LatokenOrderStatus.PartiallyFilled;
		case FILLED:
			return LatokenOrderStatus.Filled;
		case CANCELED:
		case STOPPED:
		case EXPIRED:
		case REJECTED:
		case REPLACED:
			return LatokenOrderStatus.Cancelled;
		default:
			throw new RuntimeException("Not supported order status: " + status);
		}
	}
}
