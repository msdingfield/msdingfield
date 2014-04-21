package msdingfield.easyflow.annotations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import msdingfield.easyflow.annotations.SystemBuilder;
import msdingfield.easyflow.core.FlowContext;
import msdingfield.easyflow.core.FlowEvaluator;
import msdingfield.easyflow.core.FlowSystem;

import org.junit.Test;

public class TestAnnotations {
	static Class<?>[] c = {TaskA.class};
	@Test
	public void test() throws InterruptedException, ExecutionException, IOException {
		FlowSystem system = SystemBuilder.from("com.mmaisoft.easyflow");
		FlowContext context = new FlowContext();
		context.putAttribute("input", 1);
		FlowEvaluator.evaluate(system, context).get();
		assertEquals(3, context.getAttribute("a"));
		assertEquals(6, context.getAttribute("b"));
	}

}
