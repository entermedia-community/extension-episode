package org.openedit.entermedia.episode;


public class EpisodeJob {

	
	protected int fieldReason; //the reason returned by the episode server
	protected int fieldCurrentStatus; //current status
	protected int fieldProgress; //current percentage complete
	protected int fieldJobId;
	public static final int CREATED = 0;
	public static final int QUEUED = 1;
	public static final int RUNNING = 2;
	public static final int STOPPED = 3;
	public static final int FINISHED = 4;
	public static final int FAILED = 5;
	
	
	
	
	
	public int getReason() {
		return fieldReason;
	}
	public void setReason(int inReason) {
		fieldReason = inReason;
	}
	public int getJobId() {
		return fieldJobId;
	}
	public void setJobId(int inJobId) {
		fieldJobId = inJobId;
	}
	public int getProgress() {
		return fieldProgress;
	}
	public void setProgress(int inProgess) {
		fieldProgress = inProgess;
	}
	
	public int getCurrentStatus() {
		return fieldCurrentStatus;
	}
	public void setCurrentStatus(int inCurrentStatus) {
		fieldCurrentStatus = inCurrentStatus;
	}
	public String getSettingsPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("episode status: ");
		buffer.append(" jobid: " + getJobId());
		buffer.append(" status: " + getCurrentStatus());
		buffer.append(" progress: " + getProgress());
		buffer.append(" progress: " + getProgress());
		return buffer.toString();
	}
}
