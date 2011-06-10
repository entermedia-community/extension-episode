package org.openedit.entermedia.episode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.data.SearcherManager;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.BaseCreator;
import org.openedit.entermedia.creator.ConvertInstructions;
import org.openedit.entermedia.creator.ConvertResult;
import org.openedit.entermedia.creator.MediaCreator;

import com.openedit.ModuleManager;
import com.openedit.OpenEditRuntimeException;
import com.openedit.page.Page;

public class EpisodeVideoCreator extends BaseCreator implements MediaCreator {
	private static final Log log = LogFactory.getLog(EpisodeVideoCreator.class);
	

	protected ModuleManager fieldModuleManager;
	protected SearcherManager fieldSearcherManager;
	protected EpisodeManager fieldEpisodeManager;
	
	public EpisodeManager getEpisodeManager() {
		return fieldEpisodeManager;
	}



	public void setEpisodeManager(EpisodeManager inEpisodeManager) {
		fieldEpisodeManager = inEpisodeManager;
	}



	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();

		Page inputpage = inArchive.findOriginalMediaByType("video",inAsset);
		
		if( inputpage == null || !inputpage.exists())
		{
			//no such original
			result.setOk(false);
			//inAsset.setProperty("episodeconversionstatus", "failed");
			return result;
		}
		
		
				
				
	
		
		if (inStructions.isForce() )
		{
			log.info("submitting this to episode for transcoding");
		
			
			try {
			
				getEpisodeManager().updateAsset(inArchive, inAsset.getId(), null);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new OpenEditRuntimeException(e);
			}
			
		}
		return result;
	}

	

	public SearcherManager getSearcherManager() {
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager) {
		fieldSearcherManager = inSearcherManager;
	}


	

	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}

	public boolean canReadIn(MediaArchive inArchive, String inInputType) {
		// TODO Auto-generated method stub
		return false;
	}


	


	public String populateOutputPath(MediaArchive inArchive,
			ConvertInstructions inStructions) {
		// TODO Auto-generated method stub
		return null;
	}

}
