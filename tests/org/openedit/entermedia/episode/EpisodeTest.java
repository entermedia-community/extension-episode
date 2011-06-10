package org.openedit.entermedia.episode;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openedit.entermedia.Asset;
import org.openedit.entermedia.BaseEnterMediaTest;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.ConvertInstructions;

public class EpisodeTest extends BaseEnterMediaTest
{

	public EpisodeTest(String inArg0)
	{
		super(inArg0);
	}

	protected void setUp() throws Exception
	{
		System.setProperty("oe.root.path", "../sf-redesign/webapp");

	}

	public void testApi() throws Exception
	{

		EpisodeConnection manager = (EpisodeConnection) getFixture().getModuleManager().getBean("sfcreative/browse/stockvideo", "episodeConnection");

		//UNITOPR.UNITINT.TEST.STATEFARM.ORG											// is
		// test
		// file
		String testsettings = "/Templates/SFMLS/wmv384.setting";
		// manager.setServerUrl("http://mtsc2j6v.unitopr.unitint.test.statefarm.org:40406/");
		//manager.setServerUrl("http://10.250.220.211:40406/RPC2"); // the URL

		List settings = manager.listSettings("/Templates/SFMLS/");

		//This one worked!
		String media_url = "/Volumes/Drive 1/temp/Wreck.mov"; // this
		
		Hashtable options = new Hashtable();
		options.put("dep-dstpath", "/Volumes/Drive 1/temp/");
		options.put("name", "test");
		EpisodeJob status = manager.submitJob(media_url, "3", testsettings, 500, options);

		Map metadata = manager.getMetaData(media_url);

		List jobs = manager.getJobs(true);
		// Monitor progress
		EpisodeJob progress = manager.getJob(status.getJobId());
		Thread.sleep(1000);
		EpisodeJob progess2 = manager.getJob(status.getJobId());
		//progress = manager.getJob(3);
		assertNotSame(progress.getProgress(), progess2.getProgress());

	}

	

	public void testMetaDataExtraction() throws Exception
	{

		// manager.setServerUrl();

		MediaArchive archive = getMediaArchive("entermedia/browse/testcatalog");
		EpisodeManager manager = (EpisodeManager) getFixture().getModuleManager().getBean("entermedia/browse/testcatalog", "episodeManager");
		Asset asset = archive.getAsset("testvideo");
		manager.populateMetaData(archive, "testvideo");
		assertNotNull(asset.get("aspectratio"));
	

	}

	
}
