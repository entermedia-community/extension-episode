package org.openedit.entermedia.episode;

/*package net.telestream.episode.engine.sdk.samples;*/

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcStruct;

import com.openedit.OpenEditRuntimeException;
import com.openedit.page.manage.PageManager;

public class EpisodeConnection {

	/*
	 * Available calls: connect Connect to an Episode Engine server. disconnect
	 * Disconnect from Episode Engine. getDepots Get available storage depots.
	 * getSettings Get names of available settings. getSettingsInGroupAtPath Get
	 * names of available settings for a given settings group.
	 * getSettingDataAtPath Get contents of named setting. getSettingNodeAtPath
	 * Get pointer to named settings. newSettingGroupWithNameInGroup Create
	 * settings group. newSettingWithNameSettingDataInGroup Create settings
	 * group. deleteSettingGroupAtPath Remove settings group from Episode Engine
	 * server. deleteSettingAtPath Remove named settings. getJobForID Get
	 * pointer to job data for given job ID. getJobs Get pointers to all
	 * available jobs.
	 */

	
	protected String fieldTempFolder;
	protected String fieldEpisodeRoot;
	public String getEpisodeRoot()
	{
		return fieldEpisodeRoot;
	}

	public void setEpisodeRoot(String inEpisodeRoot)
	{
		fieldEpisodeRoot = inEpisodeRoot;
	}

	public String getTempFolder()
	{
		return fieldTempFolder;
	}

	public void setTempFolder(String inTempFolder)
	{
		fieldTempFolder = inTempFolder;
	}

	public String getCatalogId() {
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId) {
		fieldCatalogId = inCatalogId;
		try{getRPCclient();
		
		} catch (Exception e) {
			
		}
	}

	
    protected XmlArchive fieldXmlArchive;
  
	
	

	public XmlArchive getXmlArchive() {
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive) {
		fieldXmlArchive = inXmlArchive;
	}

	protected String fieldCatalogId;
	
	public PageManager getPageManager() {
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager) {
		fieldPageManager = inPageManager;
	}

	protected XmlRpcClient fieldRPCclient;
	private static final Log log = LogFactory.getLog(EpisodeConnection.class);
	protected PageManager fieldPageManager;

	public EpisodeJob submitJob(String media_url, String inTaskName,
			String inSettingsPath, int inPriority, Hashtable inMetadata)
			throws Exception {
		log.info("Submitting to episode..." + media_url);
		log.info("Task Name..." + inTaskName);
		log.info("Settings Path..." + inSettingsPath);
		log.info("Priority" + inPriority);
		
		
		
		//String setting_path = "/Templates/By Format/DV/PAL/DV25_PAL.setting";
		
		// int prio = 500;

		Integer newJobID = (Integer) getRPCclient().invoke(
				"engine.submitJob",
				new Object[] { media_url, inSettingsPath, inTaskName,
						inPriority, inMetadata });
		log.info("submitted job id: " + newJobID);

	
		EpisodeJob status = getJob(newJobID);


		return status;
	}

	public List listSettings(String inTemplatePath) throws Exception {

		log.info("Settings at path: " + inTemplatePath);

		XmlRpcArray settings = (XmlRpcArray) getRPCclient().invoke(
				"engine.getSettingsInGroupAtPath",
				new Object[] { inTemplatePath });
		List set = new ArrayList();
		for (Object setting : settings) {
			System.out.println("    "
					+ ((XmlRpcStruct) setting).getString("name"));
			String name = ((XmlRpcStruct) setting).getString("name");
			set.add(name);

		}
		return set;
	}

	public List getJobs(boolean includeHistory) throws Exception {

		XmlRpcArray settings;
		if (!includeHistory) {
			settings = (XmlRpcArray) getRPCclient().invoke("engine.getJobs",
					new Object[] {});
		} else {
			settings = (XmlRpcArray) getRPCclient().invoke("engine.getJobs",
					new Object[] { true });
		}

		List set = new ArrayList();
		for (Object setting : settings) {
			XmlRpcStruct response = (XmlRpcStruct) setting;
			EpisodeJob job = getEpisodeJob(response);
			set.add(job);

		}
		return set;
	}



	public XmlRpcClient getRPCclient() throws Exception {
		
		
		if (fieldRPCclient == null) {
		
			XmlFile settings = getXmlArchive().getXml("/"+ getCatalogId() + "/configuration/episode.xml");
			if(!settings.isExist()){
				throw new OpenEditRuntimeException("Cannot start episode - please create an epsiode.xml file in" + getCatalogId() +"/configuration/");
			}
			Element config = settings.getElement("episode");
			String serverurl = config.element("server").getText();			
			String folderpath = config.element("tempfolder").getText();
			String episoderoot = config.element("episoderoot").getText();
			fieldRPCclient = new XmlRpcClient(serverurl, false);
			setTempFolder(folderpath);
			setEpisodeRoot(episoderoot);
			
		}

		
		
		return fieldRPCclient;
	}

	public void setRPCclient(XmlRpcClient inRPCclient) {
		fieldRPCclient = inRPCclient;
	}

	public EpisodeJob getJob(int inJobid) throws Exception {
		log.info("connecting to episode and getting job info for: "+ inJobid);
		XmlRpcStruct response = (XmlRpcStruct) getRPCclient().invoke(
				"engine.getJobForID", new Object[] { inJobid });
		log.info(response);
		EpisodeJob status = getEpisodeJob(response);
		return status;
	}

	private EpisodeJob getEpisodeJob(XmlRpcStruct response) {
		EpisodeJob job = new EpisodeJob();
		Object id = response.get("jobID");
		Integer jobId = null;
		if(id != null){
			jobId = (Integer) id;
		} else{
			return null;
		}
		 
		job.setJobId(jobId);
		XmlRpcStruct status = response.getStruct("currentStatus");

		int current = status.getInteger("state");
		int progress = status.getInteger("progress");
		int reason = status.getInteger("reason");
		if(current == job.FINISHED){
			progress = 100;
		}
		job.setCurrentStatus(current);
		job.setReason(reason);
		job.setProgress(progress);
		
		// 0 = Created 1 = Queued 2 = Running 3 = Stopped 4 = Finished 5 =
		// Failed
		return job;
	}

	public Map getMetaData(String media_url) throws Exception
	{
		log.info("Getting metadata for: " + media_url);
		
		
		
		//String setting_path = "/Templates/By Format/DV/PAL/DV25_PAL.setting";
		
		// int prio = 500;

		XmlRpcStruct response =  (XmlRpcStruct) getRPCclient().invoke(
				"engine.analyzeFile",
				new Object[] { media_url });
		log.info(response);
		if(response.getArray("tracks") == null){
			return null;
		}
		int width = response.getArray("tracks").getStruct(0).getInteger("width"); 
		int height = response.getArray("tracks").getStruct(0).getInteger("height"); 
		double framerate = response.getArray("tracks").getStruct(0).getDouble("frameRate");
		double duration =  response.getArray("tracks").getStruct(0).getDouble("duration");
		double offset =  response.getArray("tracks").getStruct(0).getDouble("offset");
		double aspectRatioHeight =  response.getArray("tracks").getStruct(0).getDouble("aspectRatioHeight");
		double aspectRatioWidth =  response.getArray("tracks").getStruct(0).getDouble("aspectRatioWidth");
		Hashtable map = new Hashtable();
		map.put("width", width);
		map.put("height", height);
		map.put("framerateexact", framerate);
		map.put("duration", duration);
		map.put("offset", offset);
		map.put("aspectRatioHeight", aspectRatioHeight);
		map.put("aspectRatioWidth", aspectRatioWidth);
		
		
		
		
		
		return map;
		
	}

}