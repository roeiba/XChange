package org.knowm.xchange.binancemargin.service;

import static org.knowm.xchange.binancemargin.BinanceResilience.REQUEST_WEIGHT_RATE_LIMITER;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.binancemargin.BinanceAuthenticated;
import org.knowm.xchange.binancemargin.BinancemarginExchange;
import org.knowm.xchange.binancemargin.dto.BinanceException;
import org.knowm.xchange.binancemargin.dto.account.*;
import org.knowm.xchange.client.ResilienceRegistries;

public class BinanceAccountServiceRaw extends BinanceBaseService {

  public BinanceAccountServiceRaw(
      BinancemarginExchange exchange,
      BinanceAuthenticated binance,
      ResilienceRegistries resilienceRegistries) {
    super(exchange, binance, resilienceRegistries);
  }

  public BinancemarginAccountInformation marginaccount() throws BinanceException, IOException {
    return decorateApiCall(
            () -> binance.marginAccount(getRecvWindow(), getTimestampFactory(), apiKey, signatureCreator))
        .withRetry(retry("account"))
        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), 5)
        .call();
  }
  
  public BinanceAccountInformation account() throws BinanceException, IOException {
    return decorateApiCall(
            () -> binance.account(getRecvWindow(), getTimestampFactory(), apiKey, signatureCreator))
        .withRetry(retry("account"))
        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER), 5)
        .call();
  }

  public AssetDetailResponse requestAssetDetail() throws IOException {
    return decorateApiCall(
            () ->
                binance.assetDetail(
                    getRecvWindow(), getTimestampFactory(), apiKey, signatureCreator))
        .withRetry(retry("assetDetail"))
        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
        .call();
  }

  
  public AssetDribbletLogResponse.AssetDribbletLogResults getAssetDribbletLog()
      throws BinanceException, IOException {
    return decorateApiCall(
            () ->
                binance.userAssetDribbletLog(
                    getRecvWindow(), getTimestampFactory(), super.apiKey, super.signatureCreator))
        .withRetry(retry("userAssetDribbletLog"))
        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
        .call()
        .getData();
  }

  public List<AssetDividendResponse.AssetDividend> getAssetDividend(Long startTime, Long endTime)
      throws BinanceException, IOException {
    return getAssetDividend("", startTime, endTime);
  }

  public List<AssetDividendResponse.AssetDividend> getAssetDividend(
      String asset, Long startTime, Long endTime) throws BinanceException, IOException {
    return decorateApiCall(
            () ->
                binance.assetDividend(
                    asset,
                    startTime,
                    endTime,
                    getRecvWindow(),
                    getTimestampFactory(),
                    super.apiKey,
                    super.signatureCreator))
        .withRetry(retry("assetDividend"))
        .withRateLimiter(rateLimiter(REQUEST_WEIGHT_RATE_LIMITER))
        .call()
        .getData();
  }
}
