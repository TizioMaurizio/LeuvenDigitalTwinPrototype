package Utility;

import java.util.Timer;
import java.util.TimerTask;

/**
 * prints regularly
 */
public class ScheduledPrinter{
	private Timer timer;

	/**
	 * prints a message, composed of a
	 * @param warning sub message and a
	 * @param pendingAction sub message, after a
	 * @param startDelay in ms, for a certain
	 * @param period ms and for given
	 * @param nOfRepeats times
	 */
	public void print(String warning, String pendingAction, long startDelay, long period, int nOfRepeats) {
		long beginTime=System.currentTimeMillis();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if(nOfRepeats-((System.currentTimeMillis()-beginTime-startDelay)/1000)<0) {
					timer.cancel();
					timer.purge();
				}
				System.out.print(warning+(nOfRepeats-((System.currentTimeMillis()-beginTime-startDelay)/1000)+pendingAction));
			}
		}, startDelay, period);
	}

	public void terminate(){
		this.timer.cancel();
		this.timer.purge();
	}
}
