package org.knowm.xchange.latoken.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.latoken.LatokenAdapters;
import org.knowm.xchange.latoken.LatokenErrorAdapter;
import org.knowm.xchange.latoken.dto.LatokenException;
import org.knowm.xchange.latoken.dto.account.LatokenBalance;
import org.knowm.xchange.service.account.AccountService;

public class LatokenAccountService extends LatokenAccountServiceRaw implements AccountService {

	public static final BigDecimal tradingFee = new BigDecimal("0.0025");
	
	public LatokenAccountService(Exchange exchange) {
		super(exchange);
	}
  
	@Override
	public AccountInfo getAccountInfo() throws IOException {
		
		try {
			List<List<LatokenBalance>> latokenWallets = getLatokenBalances();
			List<Wallet> wallets = new ArrayList<>();
			
			for (List<LatokenBalance> latokenWallet : latokenWallets) {
				List<Balance> balances = latokenWallet.stream()
					.map(latokenBalance -> LatokenAdapters.adaptBalance(latokenBalance))
					.collect(Collectors.toList());
				wallets.add(new Wallet(balances));
			}
			
			return new AccountInfo(null, tradingFee, wallets, new Date());
		} catch (LatokenException e) {
			throw LatokenErrorAdapter.adapt(e);
		}
	}
}
