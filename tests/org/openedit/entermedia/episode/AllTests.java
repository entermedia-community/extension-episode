package org.openedit.entermedia.episode;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for entermedia" );
		
		suite.addTestSuite( EpisodeTest.class );
		
		return suite;
	}
}
