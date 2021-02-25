package org.knowm.xchange.binancemargin.dto.account;

import java.math.BigDecimal;
import lombok.Data;

@Data
public final class TransferSubUserHistory {

  private String counterParty;
  private String email;
  private Integer type;
  private String asset;
  private BigDecimal qty;
  private long time;
}
