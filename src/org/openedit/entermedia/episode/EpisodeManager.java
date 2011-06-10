package org.openedit.entermedia.episode;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.data.XmlFileSearcher;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.event.WebEvent;
import org.openedit.event.WebEventListener;
import org.openedit.util.DateStorageUtil;
import org.openedit.xml.ElementData;

import com.openedit.ModuleManager;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.page.Page;
import com.openedit.users.User;
import com.openedit.util.FileUtils;

public class EpisodeManager
{

	protected SearcherManager fieldSearcherManager;
	protected ModuleManager fieldModuleManager;

	public WebEventListener getWebEventListener()
	{
		return fieldWebEventListener;
	}

	public void setWebEventListener(WebEventListener inWebEventListener)
	{
		fieldWebEventListener = inWebEventListener;
	}

	protected WebEventListener fieldWebEventListener;

	private static final Log log = LogFactory.getLog(EpisodeManager.class);

	public EpisodeConnection getEpisodeConnection(String inCatalogId)
	{
		return (EpisodeConnection) getModuleManager().getBean(inCatalogId, "episodeConnection");
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public void submitData(MediaArchive inArchive, Asset inAsset, ElementData taskinfo, User inUser) throws Exception
	{

		String settingspath = taskinfo.get("settingspath");

		
		String priority = taskinfo.get("priority");
		int prio;
		if (priority != null)
		{
			prio = Integer.parseInt(priority);
		}
		else
		{
			prio = 500;
		}
		Page inputpage = inArchive.getOriginalDocument(inAsset);

		//if (inputpage == null || !inputpage.exists()) {
		//					Page importasset = inArchive.getPageManager().getPage(inArchive.getCatalogHome() + "assets/" + inAsset.getSourcePath() + file.getName() );
		//					String finalpath = importasset.getContentItem().getAbsolutePath();
		//					File finalfile = new File(finalpath);
		//					FileUtils utils = new FileUtils();
		//					utils.copyFiles(file, finalfile);
		//					
		//				}

		String finalpath = copyToEpisode(inArchive, inAsset, false);
		log.info("copied asset to Episode at: " + finalpath);

		EpisodeJob status = getEpisodeConnection(inArchive.getCatalogId()).submitJob(finalpath, inAsset.getId() + taskinfo.getId(), settingspath, prio, new Hashtable());

		logStatusChange(inArchive, status, inAsset, settingspath, inUser);

	}

	public String copyToEpisode(MediaArchive inArchive, Asset inAsset, boolean overwrite) throws Exception
	{

		boolean alreadyonepisode = false;

		String temppath = inAsset.getProperty("temporiginalpath");
		String tempfolder = getEpisodeConnection(inArchive.getCatalogId()).getTempFolder();
		String root = getEpisodeConnection(inArchive.getCatalogId()).getEpisodeRoot();

		//		if(temppath != null){
		//			return temppath;
		//		}

		if (temppath != null)
		{
			File file = new File(temppath);
			alreadyonepisode = true;
			if (alreadyonepisode)
			{
				String episodepath = temppath.replace(tempfolder, root);
				episodepath = episodepath.replace("\\", "/");
				return episodepath;
			}
		}

		Page inputpage = inArchive.getOriginalDocument(inAsset);
		String abspath = inputpage.getContentItem().getAbsolutePath();

		File rootfolder = new File(tempfolder);
		File target = new File(abspath);

		File remotelocation = new File(rootfolder, target.getName());

		log.info(rootfolder.exists());
		if (!remotelocation.exists() && !overwrite)
		{
			if (rootfolder.exists() && target.exists())
			{
				FileUtils utils = new FileUtils();

				utils.copyFiles(target, rootfolder);
				log.info("file copied to episode ");
			}

			else
			{
				log.info("original not found");
			}
		} else{
			log.info("file already existed on remote server and overwrite was false. Skipping.");
		}
		String finalpath = rootfolder + "/" + inputpage.getName();
		

		finalpath = finalpath.replace(tempfolder, root);
		return finalpath;
		// return
		// "smb://mtsc2j6v.opr.statefarm.org/SFMLS/temp/L2T_OPEN_022608_SAMPLE.mov";

	}

	public void updateAsset(MediaArchive inArchive, String inAssetId, User inUser) throws Exception
	{

		Searcher settingsearcher = getSearcherManager().getSearcher(inArchive.getCatalogId(), "episode");

		Searcher searcher = getSearcherManager().getSearcher(inArchive.getCatalogId(), "conversion");

		HitTracker settings = settingsearcher.getAllHits();

		for (Iterator iterator = settings.iterator(); iterator.hasNext();)
		{
			ElementData setting = (ElementData) iterator.next();
			String settingsfile = setting.get("settingspath");
			SearchQuery query = searcher.createSearchQuery();
			query.addMatches("assetid", inAssetId);
			query.addMatches("episodesettingspath", settingsfile);
			query.setSortBy("episodelastcheckedDown");
			HitTracker hits = searcher.search(query);

			boolean needssubmit = false;
			if (hits.size() == 0)
			{
				needssubmit = true;
			}
			else
			{
				Data hit =  hits.get(0);
				String episodestatus = hit.get("episodestatus");
				needssubmit = "notsubmitted".equals(episodestatus);

			}
			if (needssubmit)
			{
				Asset asset = inArchive.getAsset(inAssetId);
				log.info("submitting asset: " + asset.getId() + " to episode");
				submitData(inArchive, asset, setting, inUser);
				continue;
			}

			Data hit = hits.get(0);
			String jobid = hit.get("episodejobid");
			String episodestatus = hit.get("episodestatus");
			Asset asset = inArchive.getAsset(inAssetId);
			log.info("asset  " + asset + " episode status " + episodestatus + "jobid: " + jobid);
			
			//0, 1, and 2 are running statuses
			if (jobid != null && "0".equals(episodestatus) || "1".equals(episodestatus) || "2".equals(episodestatus))
			{

				EpisodeJob status = getEpisodeConnection(inArchive.getCatalogId()).getJob(Integer.parseInt(jobid));
				if (status != null)
				{
					logStatusChange(inArchive, status, asset, settingsfile, inUser);
				}
				else
				{
					log.info("unable to find job: " + jobid + " in episode - resetting this job");
				    Data entry = (Data) searcher.searchById(hit.get("id"));
				    searcher.delete(entry, inUser);
				    
				}
			}

		}

	}

	public void updateAssets(MediaArchive inArchive, User inUser) throws Exception
	{
		HitTracker allassets = inArchive.getAssetSearcher().getAllHits();
		for (Iterator iterator = allassets.iterator(); iterator.hasNext();)
		{
			Document hit = (Document) iterator.next();
			String assetid = hit.get("id");
			updateAsset(inArchive, assetid, inUser);
		}

	}


	
	
	
	public void logStatusChange(MediaArchive inArchive, EpisodeJob inStatus, Asset inAsset, String inSettings, User inUser)
	{

		log.info("logging status change for " + inAsset + inSettings);
		log.info(inStatus);

		XmlFileSearcher searcher = (XmlFileSearcher) getSearcherManager().getSearcher(inArchive.getCatalogId(), "conversion");
		Data conversion = null;
		SearchQuery query = searcher.createSearchQuery();
		query.addMatches("episodesettingspath", inSettings);
		query.addMatches("assetid", inAsset.getId());
		HitTracker hits = searcher.search(query);
		if(hits.size() >0){
			String id = hits.get(0).get("id");
			conversion = (Data) searcher.searchById(id);
		} 
		
		
		if (conversion == null)
		{
			conversion = searcher.createNewData();
			log.info("adding new conversion entry for " + inStatus.getJobId() + "asset: " + inAsset.getId());
		}
		PropertyDetail detail = searcher.getDetail("episodelastchecked");
		Date now = new Date();
		if (detail != null)
		{
			String lastchecked =DateStorageUtil.getStorageUtil().formatForStorage(now);
			conversion.setProperty("episodelastchecked", lastchecked);
		}
		//conversion.setId(String.valueOf(inStatus.getJobId()));
		
		conversion.setSourcePath(inAsset.getSourcePath());
		conversion.setProperty("assetid", inAsset.getId());
		conversion.setProperty("episodejobid", String.valueOf(inStatus.getJobId()));
		conversion.setProperty("episodestatus", String.valueOf(inStatus.getCurrentStatus()));
		conversion.setProperty("episodeprogress", String.valueOf(inStatus.getProgress()));
		conversion.setProperty("episodesettingspath", inSettings);
		conversion.setProperty("episodereason", String.valueOf(inStatus.getReason()));
		searcher.saveData(conversion, null);
		// now fire an event based on this. The scripts can handle the change.

		WebEvent event = new WebEvent();
		event.setSource(this);
		event.setCatalogId(inArchive.getCatalogId());

		if (inStatus.getCurrentStatus() == inStatus.FINISHED)
		{
			event.setOperation("episodefinished");
		}
		if (inStatus.getCurrentStatus() == inStatus.FAILED)
		{
			event.setOperation("episodefailed");
		}

		log.info("Firing event " + event.getOperation());
		event.setProperty("assetid", inAsset.getId());
		event.setProperty("episodeprogress", String.valueOf(inStatus.getProgress()));
		event.setProperty("episodestatus", String.valueOf(inStatus.getCurrentStatus()));
		event.setProperty("episodejobid", String.valueOf(inStatus.getJobId()));
		event.setProperty("episodesettingspath", inSettings);
		event.setProperty("episodereason", String.valueOf(inStatus.getReason()));
		event.setUser(inUser);
		log.info(getWebEventListener());
		getWebEventListener().eventFired(event);
	}

	public void populateMetaData(MediaArchive inArchive, String inAssetId) throws Exception
	{
		Asset asset = inArchive.getAsset(inAssetId);

		String filepath = copyToEpisode(inArchive, asset, false);
		Searcher settingsearcher = getSearcherManager().getSearcher(inArchive.getCatalogId(), "episode");

		Map map = getEpisodeConnection(inArchive.getCatalogId()).getMetaData(filepath);
		if (map != null)
		{
			for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();
				String stringval = String.valueOf(map.get(key));
				asset.setProperty(key, stringval);
			}

			Integer height = (Integer) map.get("height");
			Integer width = (Integer) map.get("width");
			float aspect = width.floatValue() / height.floatValue();
			asset.setProperty("aspectratioexact", String.valueOf(aspect));

			float fourthree = 4f / 3f; //1.333
			float sixteennine = 16f / 9f; //1.999

			if (Math.abs(aspect - fourthree) < Math.abs(aspect - sixteennine))
			{
				asset.setProperty("aspectratio", "43");
			}
			else
			{
				asset.setProperty("aspectratio", "169");
			}

			Double framerate = (Double) map.get("framerateexact");

			if (Math.abs(framerate - 24) < Math.abs(framerate - 30))
			{
				asset.setProperty("framerate", "24");
			}
			else
			{
				asset.setProperty("framerate", "30");
			}

		}

	}

}
