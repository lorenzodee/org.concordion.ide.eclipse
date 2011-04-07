package org.concordion.ide.eclipse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Runs asynchronous tasks and jobs
 */
public class JobRunner {
	/**
	 * Interface for tasks throwing exceptions
	 */
	public static interface Task {
		void run() throws Throwable;
	}
	
	/**
	 * Adapter from {@link Task} to {@link Runnable}, logging exceptions
	 */
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
	
	/** Runs a synchronous task, showing a busy indicator */
	public static void runSync(Task task, String name) {
		runSync(new RunnableTask(task, name));
	}
	
	/** Runs a synchronous task, showing a busy indicator */
	public static void runSync(Runnable run) {
		BusyIndicator.showWhile(Display.getDefault(), run);
	}
	
	/** Runs an asynchronous task as a {@link Job} */
	public static void runJob(Task task, String name) {
		runJob(new RunnableTask(task, name), name);
	}
	
	/** Runs an asynchronous task as a {@link Job} */
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
