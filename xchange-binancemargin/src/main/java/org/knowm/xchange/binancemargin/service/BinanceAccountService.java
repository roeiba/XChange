package org.knowm.xchange.binancemargin.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.knowm.xchange.binancemargin.BinanceAuthenticated;
import org.knowm.xchange.binancemargin.BinanceErrorAdapter;
import org.knowm.xchange.binancemargin.BinancemarginExchange;
import org.knowm.xchange.binancemargin.dto.BinanceException;
import org.knowm.xchange.binancemargin.dto.account.AssetDetail;
import org.knowm.xchange.binancemargin.dto.account.BinanceAccountInformation;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.service.account.AccountService;

public class BinanceAccountService extends BinanceAccountServiceRaw implements AccountService {

  public BinanceAccountService(
      BinancemarginExchange exchange,
      BinanceAuthenticated binance,
      ResilienceRegistries resilienceRegistries) {
    super(exchange, binance, resilienceRegistries);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    try {
      BinanceAccountInformation acc = account();
      List<Balance> balances =
          acc.balances.stream()
              .map(b -> new Balance(b.getCurrency(), b.getTotal(), b.getAvailable()))
              .collect(Collectors.toList());
      return new AccountInfo(new Date(acc.updateTime), Wallet.Builder.from(balances).build());
    } catch (BinanceException e) {
      throw BinanceErrorAdapter.adapt(e);
    }
  }

  @Override
  public Map<CurrencyPair, Fee> getDynamicTradingFees() throws IOException {
    try {
      BinanceAccountInformation acc = account();
      BigDecimal makerFee =
          acc.makerCommission.divide(new BigDecimal("10000"), 4, RoundingMode.UNNECESSARY);
      BigDecimal takerFee =
          acc.takerCommission.divide(new BigDecimal("10000"), 4, RoundingMode.UNNECESSARY);

      Map<CurrencyPair, Fee> tradingFees = new HashMap<>();
      List<CurrencyPair> pairs = exchange.getExchangeSymbols();

      pairs.forEach(pair -> tradingFees.put(pair, new Fee(makerFee, takerFee)));

      return tradingFees;
    } catch (BinanceException e) {
      throw BinanceErrorAdapter.adapt(e);
    }
  }

  public Map<String, AssetDetail> getAssetDetails() throws IOException {
    try {
      return super.requestAssetDetail().getAssetDetail();
    } catch (BinanceException e) {
      throw BinanceErrorAdapter.adapt(e);
    }
  }
}
