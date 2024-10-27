import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SchedSim {

    private int timeSlice = 2;
    private ReadyQueue readyQueue = new ReadyQueue();
    private WaitingQueue waitingQueue = new WaitingQueue();
    private CPU cpu = new CPU();
    private int currentTime = 0, jobCount = 0, timeTick = 0;
    private int completedJobCount = 0, terminatedJobCount = 0, timeoutJobCount = 0;
    private int totalIdleTimeForCompletedJobs = 0, totalTurnAroundTimeForCompletedJobs = 0;
    private ExternalEvents extEvents;
    private PrintWriter out;

    public static void main(String[] args) throws IOException {
        System.out.println("Loading external events from SchedSim.txt ...");
        ExternalEvents extEvents = new ExternalEvents("./Programming_Assignment_2/src/resources/input.txt");
        PrintWriter out = null;
        try {
            System.out.println("Creating output file SchedSim.out ...");
            out = new PrintWriter(new FileWriter("./Programming_Assignment_2/src/resources/SchedSim.out"));
            System.out.println("Starting simulation ...");
            new SchedSim().startSimulation(extEvents, out);
            System.out.println("Simulation complete.");
        } finally {
            if (out != null)
                out.close();
        }
    }

    public void startSimulation(ExternalEvents extEvents, PrintWriter out) {
        this.extEvents = extEvents;
        this.out = out;

        while (currentTime <= extEvents.maxEventTime() || cpu.getRunningJob() != null || !readyQueue.isEmpty()) {
            out.println("Time is: " + currentTime);
            processExternalEvents();
            processInternalEvents();
            execute();
            currentTime++;
            out.println();
        }

        out.println("Time is: " + currentTime);
        out.println("\tNo more events to process. Terminate all waiting jobs.");
        for (int jobNo : waitingQueue.getJobsList()) {
            Job job = waitingQueue.remove(jobNo);
            out.println("\t\tTrying to terminate job #" + jobNo + ":");
            job.setExitType("Terminated");
            printJobStats(job);
            terminatedJobCount++;
        }
        out.println();

        out.println("-------------------------------------------");
        out.println("The simulation has ended.");
        out.println();
        printFinalStats();
    }

    private void processExternalEvents() {
        out.println("\tProcessing External Events ...");
        ExternalEvent extEvent = extEvents.getEvent(currentTime);

        if (extEvent == null) {
            out.println("\t\tNothing to do.");
            return;
        }

        char command = extEvent.getCommand();
        switch (command) {
            case 'J':
                handleJobCreation(extEvent);
                break;
            case 'W':
                handleJobWaiting();
                break;
            case 'R':
                handleJobReady(extEvent);
                break;
            case 'C':
                handleJobCompletion();
                break;
            case 'T':
                handleJobTermination(extEvent);
                break;
            default:
                out.println("\t\t---Unknown command.");
                break;
        }
    }

    private void handleJobCreation(ExternalEvent extEvent) {
        out.println("\t\tSpawning new job to the ready queue:");
        Job newJob = new Job(++jobCount, extEvent.getPriority(), currentTime, extEvent.getTimeEstimate());
        printJobInfo(newJob);
        readyQueue.add(newJob);

        Job runningJob = cpu.getRunningJob();
        if (runningJob != null && newJob.getPriority() < runningJob.getPriority()) {
            out.println("\t\tHigher priority job arrived. Moving the current running job to ready queue.");
            preemptRunningJob(runningJob, newJob);
        }
    }

    private void preemptRunningJob(Job runningJob, Job newJob) {
        readyQueue.add(runningJob);
        assignJobToCpu(newJob);
    }

	private void handleJobWaiting() {
		out.println("\t\tStart I/O for current job:");
		Job currentJob = cpu.getRunningJob();

		if (currentJob != null) {
			out.println("\t\tJob #" + currentJob.getJobNo() + " is moved to waiting queue.");
			waitingQueue.add(currentJob);
			cpu.setRunningJob(null); // CPU becomes idle
		} else {
			out.println("\t\t---Failed: No job is currently running.");
		}
	}

    private void handleJobReady(ExternalEvent extEvent) {
        out.println("\t\tMake waiting Job #" + extEvent.getJobNo() + " ready:");
        Job waitingJob = waitingQueue.remove(extEvent.getJobNo());

        if (waitingJob != null) {
            readyQueue.add(waitingJob);
            out.println("\t\t---Job #" + extEvent.getJobNo() + " is moved to ready queue.");
        } else {
            out.println("\t\t---Failed: Job #" + extEvent.getJobNo() + " is not waiting.");
        }
    }

    private void handleJobCompletion() {
        out.println("\t\tTrying to complete the current running job:");
        Job runningJob = cpu.getRunningJob();

        if (runningJob != null) {
            completeJob(runningJob);
        } else {
            out.println("\t\t---Failed: No job is currently running.");
        }
    }

    private void completeJob(Job job) {
        job.setExitType("Completed");
        printJobStats(job);
        completedJobCount++;
        totalIdleTimeForCompletedJobs += job.getIdleTime();
        totalTurnAroundTimeForCompletedJobs += (currentTime - job.getEntryTime());
        cpu.setRunningJob(null); // CPU is now idle
    }

    private void handleJobTermination(ExternalEvent extEvent) {
        out.println("\t\tTrying to terminate job #" + extEvent.getJobNo() + ":");
        Job runningJob = cpu.getRunningJob();

        if (runningJob != null && extEvent.getJobNo() == runningJob.getJobNo()) {
            out.println("\t\t---Failed: Cannot terminate running job.");
        } else {
            terminateJob(extEvent.getJobNo());
        }
    }

    private void terminateJob(int jobNo) {
        Job removedJob = readyQueue.remove(jobNo);
        if (removedJob == null) {
            removedJob = waitingQueue.remove(jobNo);
        }

        if (removedJob == null) {
            out.println("\t\t---Failed: Job #" + jobNo + " not found in the system.");
        } else {
            removedJob.setExitType("Terminated");
            printJobStats(removedJob);
            terminatedJobCount++;
        }
    }

	private void processInternalEvents() {
		out.println("\tProcessing Internal Events ...");
		Job currentJob = cpu.getRunningJob();

		if (currentJob == null || currentJob.getTimeRemaining() <= 0) {
			handleJobCompletionOrTimeout(currentJob);
		} else if (timeTick >= timeSlice) {
			handleTimeSliceExpiration(currentJob);
		} else {
			out.println("\t\tSlice still good.");
		}

		// Check if the CPU is idle and assign next job in queue if available
		if (cpu.getRunningJob() == null && !readyQueue.isEmpty()) {
			Job nextJob = readyQueue.remove();
			assignJobToCpu(nextJob);
			timeTick = 0; // Reset the time slice for the new job
		}
	}

	private void handleJobCompletionOrTimeout(Job currentJob) {
		if (currentJob != null && currentJob.getTimeRemaining() <= 0) {
			currentJob.setExitType("Time out");
			printJobStats(currentJob);
			timeoutJobCount++;
			cpu.setRunningJob(null); // Mark CPU as idle
		}

		Job nextJob = readyQueue.remove(); // Fetch the next job from the ready queue
		if (nextJob == null) {
			out.println("\t\tNothing is ready.");
		} else {
			if (nextJob.getTimeRemaining() <= 0) {
				handleJobCompletionOrTimeout(nextJob);
			} else{
				assignJobToCpu(nextJob); // Assign CPU to the next job
				timeTick = 0; // Reset time slice for the new job
			}
		}
	}

	private void handleTimeSliceExpiration(Job currentJob) {
		out.println("\t\tSlice expired.");
		Job nextJob = readyQueue.peek();

		// Only preempt if there's a job with equal or higher priority
		if (nextJob != null && nextJob.getPriority() < currentJob.getPriority()) {
			out.println("\t\tJob #" + currentJob.getJobNo() + " is preempted.");
			readyQueue.add(currentJob); // Move current job back to ready queue
			readyQueue.remove(nextJob); // Remove next job from the queue
			assignJobToCpu(nextJob); // Assign the CPU to the higher priority job
			timeTick = 0; // Reset time slice for the new job
		} else {
			out.println("\t\tNo higher-priority jobs ready.");
			out.println("\t\tJob #" + currentJob.getJobNo() + " keeps the CPU.");
			timeTick = 0; // Reset time slice for current job
		}
	}

    private void assignJobToCpu(Job job) {
        cpu.setRunningJob(job);
		job.incrementCpuEntryCount();
		readyQueue.remove(job); // Remove the job from the ready queue to avoid duplicates
		timeTick = 0;
		out.println("\t\tJob #" + job.getJobNo() + " gets the CPU.");
    }

    private void execute() {
        Job runningJob = cpu.getRunningJob();
        if (runningJob != null) {
            out.println("\t<<TICK>>");
            timeTick++;
            cpu.execute();
            printJobInfo(runningJob);
        } else {
            out.println("\tNothing is running.");
            timeTick = 0;
        }
        readyQueue.updateIdleTimes();
    }

    public void printJobInfo(Job job) {
        out.println("\t\t---Job #" + job.getJobNo() + ", priority = " + job.getPriority() + ", time remaining = "
                + job.getTimeRemaining());
    }

    public void printJobStats(Job job) {
        out.println("\t\t---Exit Type:  " + job.getExitType() + ".");
        out.println("\t\t---Job #" + job.getJobNo() + ", priority = " + job.getPriority() + ", time remaining = "
                + job.getTimeRemaining());
        out.println("\t\t---Running Time = " + job.getRunningTime() + ", Idle Time = " + job.getIdleTime()
                + ", Turn-Around Time = " + (currentTime - job.getEntryTime()) + ", Entered CPU "
                + job.getCpuEntryCount() + " time(s).");
    }

    public void printFinalStats() {
        out.println("Final Statistics:");
        out.println("---Number of jobs entering the system: " + jobCount);
        out.println("---Number of jobs terminated: " + terminatedJobCount);
        out.println("---Number of jobs timed out: " + timeoutJobCount);
        out.println("---Number of completed jobs: " + completedJobCount);
        out.println("---Average wait time (completed jobs only): "
                + (double) totalIdleTimeForCompletedJobs / completedJobCount);
        out.println("---Average turn-around time (completed jobs only): "
                + (double) totalTurnAroundTimeForCompletedJobs / completedJobCount);
    }
}