package org.knowm.xchange.bibox.dto.trade;

import org.knowm.xchange.bibox.dto.BiboxCommand;

/** @author roeiba */
public class BiboxPendingHistoryCommand extends BiboxCommand<BiboxPendingHistoryCommandBody> {

  public BiboxPendingHistoryCommand(BiboxPendingHistoryCommandBody body) {
    super("orderpending/pendingHistoryList", body);
  }
}
