package msdingfield.easyflow.annotations;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.annotations.Task;

@Task
public class TaskB {

	@Input
	public int a;
	
	@Output
	public int b;
	
	@Operation
	public void boog() {
		b = a * 2;
	}
}
