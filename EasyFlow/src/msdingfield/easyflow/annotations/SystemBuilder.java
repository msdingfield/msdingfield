package msdingfield.easyflow.annotations;

import java.io.IOException;
import java.util.Collection;

import msdingfield.easyflow.core.FlowOperation;
import msdingfield.easyflow.core.FlowSystem;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;

public class SystemBuilder {
	public static FlowSystem from(final Class<?> ...classes) {
		final Collection<FlowOperation> operations = Lists.newArrayList();
		for (final Class<?> clazz : classes) {
			operations.add(AnnotatedOperationWrapper.of(clazz));
		}
		return new FlowSystem(operations);
	}
	
	public static FlowSystem from(final String pkg) throws IOException {
		final Collection<FlowOperation> operations = Lists.newArrayList();
		ClassPath classPath = ClassPath.from(SystemBuilder.class.getClassLoader());
		for (final ClassPath.ClassInfo info : classPath.getTopLevelClassesRecursive(pkg)) {
			Class<?> class1 = info.load();
			if (class1.isAnnotationPresent(Task.class)) {
				operations.add(AnnotatedOperationWrapper.of(class1));
			}
		}
		return new FlowSystem(operations);
	}
}
