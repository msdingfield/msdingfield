package msdingfield.easyflowexample.dal;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class StockQuoteDao {

	public ListenableFuture<Double> getCurrentQuote(final String symbol) {
		final SettableFuture<Double> future = SettableFuture.create();
		Double quote = 0.0;
		switch(symbol) {
		case "MSFT": quote = 35.0; break;
		case "AAPL": quote = 503.0; break;
		case "GOOG": quote = 909.0; break;
		case "T": quote = 36.0; break;
		case "MMM": quote = 137.0; break;
		}
		LatencySimulator.setWithLatency(future, quote, 250L);
		return future;
	}
}
