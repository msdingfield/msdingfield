package msdingfield.easyflowexample;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.annotations.Prepare;
import msdingfield.easyflow.annotations.SystemBuilder;
import msdingfield.easyflow.core.FlowContext;
import msdingfield.easyflow.core.FlowEvaluator;
import msdingfield.easyflow.core.FlowSystem;
import msdingfield.easyflowexample.dal.PortfolioDao;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

/*
 * List of stocks + current quote + shares owned
 * Recently viewed stocks + current quote
 */
public class Main {

	public static class GetPortfolio {
		
		private PortfolioDao dao = new PortfolioDao();
		
		@Input
		public String clientId;
		
		@Output
		public ListenableFuture<Set<String>> portfolioSymbols;
		
		@Operation
		public void doLookup() {
			portfolioSymbols = dao.getPortfolio(clientId);
		}
	}

	public static void main(String[] args) {
		try {
			final FlowSystem system = SystemBuilder.from(GetPortfolio.class);
			final FlowContext context = new FlowContext();
			context.putAttribute("clientId", "123");
			FlowEvaluator.evaluate(system, context).get();
			System.out.println(context.toString());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
