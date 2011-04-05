package org.concordion.ide.eclipse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

public class JobRunner {
	public static interface Task {
		void run() throws Throwable;
	}
	
	private static class RunnableTask implements Runnable {
		private Task task;
		private String name;
		public RunnableTask(Task task, String name) {
			this.task = task;
			this.name = name;
		}
		@Override
		public void run() {
			try {
				task.run();
			} catch (Throwable t) {
				EclipseUtils.logError("Task " + name + " failed", t);
			}
		}
	}
	
	public static void runSync(Task task, String name) {
		runSync(new RunnableTask(task, name));
	}
	
	public static void runSync(Runnable run) {
		BusyIndicator.showWhile(Display.getDefault(), run);
	}
	
	public static void runJob(Task task, String name) {
		runJob(new RunnableTask(task, name), name);
	}
	
	public static void runJob(final Runnable run, String name) {
		Job job = new Job(name) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				run.run();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
