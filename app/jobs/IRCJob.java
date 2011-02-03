package jobs;

import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class IRCJob extends Job<Void> {
	
	@Override
	public void doJob() throws Exception {
		Logger.info("Starting IRC bot");
		IRCBot.start();
	}
}
