package org.knowm.xchange.bibox.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import org.knowm.xchange.bibox.dto.BiboxAdapters;

public class BiboxDeposit {

  public final long txId;
  public final String to;
  public final String coinSymbol;
  public final String confirmCount;
  public final BigDecimal amount;
  private final Date createdAt;
  public final int status;

  public BiboxDeposit(
      @JsonProperty("id") long txId,
      @JsonProperty("to_address") String to,
      @JsonProperty("coin_symbol") String coinSymbol,
      @JsonProperty("confirmCount") String confirmCount,
      @JsonProperty("amount") BigDecimal amount,
      @JsonProperty("createdAt") long createdAt,
      @JsonProperty("status") int status) {
    this.txId = txId;
    this.to = to;
    this.coinSymbol = coinSymbol;
    this.confirmCount = confirmCount;
    this.amount = amount;
    this.createdAt = BiboxAdapters.convert(createdAt);
    this.status = status;
  }

  public Date getCreatedAt() {
    return new Date(createdAt.getTime());
  }
}
