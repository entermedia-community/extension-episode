package org.openedit.entermedia.episode;

import java.util.Date;
import java.util.GregorianCalendar;

import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.modules.BaseMediaModule;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;

public class EpisodeModule extends BaseMediaModule{
	
	
	protected EpisodeManager fieldEpisodeManager;
	


	public EpisodeManager getEpisodeManager() {
		return fieldEpisodeManager;
	}



	public void setEpisodeManager(EpisodeManager inEpisodeManager) {
		fieldEpisodeManager = inEpisodeManager;
	}



	public void loadEpisodeStatus(WebPageRequest inReq){
		MediaArchive archive = getMediaArchive(inReq);
		Searcher searcher = getSearcherManager().getSearcher(
				archive.getCatalogId(), "conversion");
		//searcher.reIndexAll();
		SearchQuery query = searcher.createSearchQuery();
		String days = inReq.findValue("daystoshow");
		Date d = new Date();
		int len = Integer.parseInt(days);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.add(GregorianCalendar.DAY_OF_MONTH, 0 - len); // subtract
															// start
															// date
		d = cal.getTime();
		PropertyDetail detail = searcher.getDetail("episodelastchecked");
		query.addAfter(detail, d);
		query.addSortBy("episodelastcheckedDown");
		HitTracker hits = searcher.cachedSearch(inReq, query);
//	    HitTracker hits = searcher.getAllHits();
//	    hits.size();
	}
	

	
	
	
	
}
