package msdingfield.easyflow.annotations;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.annotations.Task;

@Task
public class TaskA {

	@Input
	public int input;
	
	@Output
	public int a;
	
	@Operation
	public void execute() {
		a = input + 2;
	}
}
