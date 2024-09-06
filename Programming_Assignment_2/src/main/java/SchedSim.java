/**
 * Project 2 - Simulation of Priority Based Round Robin Scheduling Algorithm
 */

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SchedSim {
	
	private int timeSlice = 2; 
   private ReadyQueue readyQueue = new ReadyQueue();
	private WaitingQueue waitingQueue = new WaitingQueue();
	private CPU cpu = new CPU();
	private int currentTime=0, jobCount=0, timeTick=0;
	private int completedJobCount=0, terminatedJobCount=0, timeoutJobCount=0;
	private int totalIdleTimeForCompletedJobs=0, totalTurnAroundTimeForCompletedJobs=0;
	private ExternalEvents extEvents;
	private PrintWriter out;

	public static void main(String[] args) throws IOException {
		System.out.println("Loading external events from SchedSim.txt ...");
		ExternalEvents extEvents = new ExternalEvents("SchedSim.txt");
		PrintWriter out = null;
		try {
			System.out.println("Creating output file SchedSim.out ...");
			out = new PrintWriter(new FileWriter("SchedSim.out"));
			System.out.println("Starting simulation ...");
			new SchedSim().startSimulation(extEvents,out);
			System.out.println("Simulation complete.");
		} finally {
			if(out!=null) out.close();
		}
	}
	
	public void startSimulation(ExternalEvents extEvents, PrintWriter out) {
		this.extEvents = extEvents;
		this.out = out;
		
		/* Run simulation until no more external events and no jobs in running or ready state */
		while(currentTime<=extEvents.maxEventTime() || cpu.getRunningJob()!=null || !readyQueue.isEmpty()) {
			out.println("Time is: " + currentTime);
			processExternalEvents();  // you will implement
			processInternalEvents();  
         execute();
			currentTime++;
			out.println();
		}
		
		/* Terminate all waiting jobs once the simulation is complete */
		out.println("Time is: " + currentTime);
		out.println("\tNo more events to process. Terminate all waiting jobs.");
		for(int jobNo : waitingQueue.getJobsList()) {
			Job job = waitingQueue.remove(jobNo);
			out.println("\t\tTrying to terminate job #" + jobNo + ":");
			job.setExitType("Terminated");
			printJobStats(job);
			terminatedJobCount++;
		}
		out.println();
		
		/* Print final statistics */
		out.println("-------------------------------------------");
		out.println("The simulation has ended.");
		out.println();
		printFinalStats();
	}
	
	private void processExternalEvents() {
		out.println("\tProcessing External Events ...");
		ExternalEvent extEvent = extEvents.getEvent(currentTime);
		if(extEvent==null) {
			out.println("\t\tNothing to do.");
		} else {
			char command = extEvent.getCommand();
			if(command=='J') {
				out.println("\t\tSpawning new job to the ready queue:");
				Job newJob = new Job(++jobCount, extEvent.getPriority(), currentTime, extEvent.getTimeEstimate());
				printJobInfo(newJob);
            
				/* Check if the CPU has a job running and the new job has higher priority than the currently running job[cpu.getRunningJob()]. 
               If so then preempt the currently running job and add it to the readyQ
               Assign the CPU to the newJob
             */
                  
			} else if(command=='W') {
				out.println("\t\tStart I/O for current job:");
				Job runningJob = cpu.getRunningJob();
            
            // add the runningJob to waitQ
				
           } else if(command=='R') {
				out.println("\t\tMake waiting Job #" + extEvent.getJobNo() + " ready:");
				Job waitingJob = waitingQueue.remove(extEvent.getJobNo());
				if(waitingJob!=null) {
					readyQueue.add(waitingJob);
					out.println("\t\t---Job #" + extEvent.getJobNo() + " is moved to ready queue.");
				} else {
					out.println("\t\t---Failed: Job #" + extEvent.getJobNo() + " is not waiting.");
				}
			} else if(command=='C') {
				out.println("\t\tTrying to complete the current running job:");
				Job runningJob = cpu.getRunningJob();
				if(runningJob!=null) {
					runningJob.setExitType("Completed");
					printJobStats(runningJob);
					completedJobCount++;
					totalIdleTimeForCompletedJobs += runningJob.getIdleTime();
					totalTurnAroundTimeForCompletedJobs += (currentTime-runningJob.getEntryTime());
					cpu.setRunningJob(null);
				} else {
					out.println("\t\t---Failed: No job is currently running.");
				}
			} else if(command=='T') {
				out.println("\t\tTrying to terminate job #" + extEvent.getJobNo() + ":");
				if(extEvent.getJobNo()==cpu.getRunningJob().getJobNo()) {
					out.println("\t\t---Failed: Cannot terminate running job.");
				} else {
					Job removedJob = readyQueue.remove(extEvent.getJobNo());
					if(removedJob==null)
						removedJob = waitingQueue.remove(extEvent.getJobNo());
					
					if(removedJob==null) {
						out.println("\t\t---Failed: Job #" + extEvent.getJobNo() + " not found in the system.");
					}
					else {
						removedJob.setExitType("Terminated");
						printJobStats(removedJob);
						terminatedJobCount++;
					}
				}
			}
		}
	}
	
	private void processInternalEvents() {
		out.println("\tProcessing Internal Events ...");
		Job currentJob = cpu.getRunningJob();
		if(currentJob==null || currentJob.getTimeRemaining() <= 0) {
			if(currentJob!=null) {
				currentJob.setExitType("Time out");
				printJobStats(currentJob);
				timeoutJobCount++;
			}
			Job nextJob = readyQueue.remove();
			cpu.setRunningJob(nextJob);
			if(nextJob==null) {
				out.println("\t\tNothing is ready.");
			}
			else {
				out.println("\t\tJob #" + nextJob.getJobNo() + " gets the CPU.");
				nextJob.incrementCpuEntryCount();
				timeTick=0;
			}
		} else {
			if(timeTick < timeSlice) {
				out.println("\t\tSlice still good.");
			} else {
				out.println("\t\tSlice expired.");
				Job nextJob = readyQueue.peek();
				if(nextJob==null || nextJob.getPriority() > currentJob.getPriority()) {
					out.println("\t\tNo jobs of equal or higher priority ready.");
					out.println("\t\tJob #" + currentJob.getJobNo() + " keeps the CPU.");
					timeTick=0;
				}
				else {
					readyQueue.add(currentJob);
					out.println("\t\tJob #" + currentJob.getJobNo() + " is moved to the ready queue.");
					out.println("\t\tJob #" + nextJob.getJobNo() + " gets the CPU.");
					readyQueue.remove(nextJob);
					cpu.setRunningJob(nextJob);
					nextJob.incrementCpuEntryCount();
					timeTick=0;
				}
			}
		}
	}
	
	private void execute() {
		Job runningJob = cpu.getRunningJob();
		if(runningJob!=null) {
			out.println("\t<<TICK>>");
			timeTick++;
			cpu.execute();
			printJobInfo(runningJob);
		} else {
			out.println("\tNothing is running.");
			timeTick=0;
		}
		readyQueue.updateIdleTimes();
	}
	
	public void printJobInfo(Job job) {
		out.println("\t\t---Job #" + job.getJobNo() + ", priority = " + job.getPriority() + ", time remaining = " + job.getTimeRemaining());
	}
	
	public void printJobStats(Job job) {
		out.println("\t\t---Exit Type:  " + job.getExitType() + ".");
		out.println("\t\t---Job #" + job.getJobNo() + ", priority = " + job.getPriority() + ", time remaining = " + job.getTimeRemaining());
		out.println("\t\t---Running Time = " + job.getRunningTime() + ", Idle Time = " + job.getIdleTime()
				+ ", Turn-Around Time = " + (currentTime-job.getEntryTime()) + ", Entered CPU " + job.getCpuEntryCount() + " time(s).");
	}
	
	public void printFinalStats() {
		out.println("Final Statistics:");
		out.println("---Number of jobs entering the system: " + jobCount);
		out.println("---Number of jobs terminated: " + terminatedJobCount);
		out.println("---Number of jobs timed out: " + timeoutJobCount);
		out.println("---Number of completed jobs: " + completedJobCount);
		out.println("---Average wait time (completed jobs only): " + (double)totalIdleTimeForCompletedJobs/completedJobCount);
		out.println("---Average turn-around time (completed jobs only): " + (double)totalTurnAroundTimeForCompletedJobs/completedJobCount);
	}
}
