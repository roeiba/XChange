package org.knowm.xchange.binancemargin.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public final class BinancemarginAccountInformation {

  public final BigDecimal marginLevel;
  public final BigDecimal totalAssetOfBtc;
  public final BigDecimal totalLiabilityOfBtc;
  public final BigDecimal totalNetAssetOfBtc;
  public final boolean borrowEnabled;
  public final boolean tradeEnabled;
  public final boolean transferEnabled;
  public List<BinancemarginAsset> userAssets;

  public BinancemarginAccountInformation(
      @JsonProperty("marginLevel") BigDecimal marginLevel,
      @JsonProperty("totalAssetOfBtc") BigDecimal totalAssetOfBtc,
      @JsonProperty("totalLiabilityOfBtc") BigDecimal totalLiabilityOfBtc,
      @JsonProperty("totalNetAssetOfBtc") BigDecimal totalNetAssetOfBtc,
      @JsonProperty("borrowEnabled") boolean borrowEnabled,
      @JsonProperty("tradeEnabled") boolean tradeEnabled,
      @JsonProperty("transferEnabled") boolean transferEnabled,
      @JsonProperty("userAssets") List<BinancemarginAsset> assets) {
    this.marginLevel = marginLevel;
    this.totalAssetOfBtc = totalAssetOfBtc;
    this.totalLiabilityOfBtc = totalLiabilityOfBtc;
    this.totalNetAssetOfBtc = totalNetAssetOfBtc;
    this.borrowEnabled = borrowEnabled;
    this.tradeEnabled = tradeEnabled;
    this.transferEnabled = transferEnabled;
    this.userAssets = assets;
  }
}
