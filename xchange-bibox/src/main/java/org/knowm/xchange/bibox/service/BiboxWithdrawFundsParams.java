package org.knowm.xchange.bibox.service;

import java.math.BigDecimal;
import javax.annotation.Nullable;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.service.trade.params.DefaultWithdrawFundsParams;

public class BiboxWithdrawFundsParams extends DefaultWithdrawFundsParams {

  @Nullable public final String tradePassword; // optional
  @Nullable public final int googleAuth; // optional
  @Nullable public final String remark; // optional

  public BiboxWithdrawFundsParams(String address, Currency currency, BigDecimal amount) {
    this(address, currency, amount, null, null, null);
  }

  public BiboxWithdrawFundsParams(
      String address, Currency currency, BigDecimal amount, String password, Integer googleAuthCode, String remark) {
    super(address, currency, amount);
    this.tradePassword = password;
    this.googleAuth = googleAuthCode;
    this.remark = remark;
  }

  @Override
  public String toString() {
    return "BiboxWithdrawFundsParams{"
        + "address='"
        + getAddress()
        + '\''
        + ", currency="
        + getCurrency()
        + ", amount="
        + getAmount()
        + ", auth="
        + getGoogleAuth()
        + ", tradepass="
        + getTradePassword()
        + ", remark="
        + getRemark()
        + '}';
  }

  @Nullable
  public String getTradePassword() {
    return tradePassword;
  }

  @Nullable
  public int getGoogleAuth() {
    return googleAuth;
  }

  @Nullable
  public String getRemark() {
    return remark;
  }

}
