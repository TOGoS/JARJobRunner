package togos.jarjobrunner;

import java.net.MalformedURLException;
import java.net.URL;

public class BlobOracle
{
	/** http://..../uri-res/ */
	protected final String repositoryPrefix;
	
	public BlobOracle( String repositoryPrefix ) {
		this.repositoryPrefix = repositoryPrefix;
	}
	
	public URL getBlobUrl( String uri, String defaultFilename ) throws MalformedURLException {
		return new URL(repositoryPrefix + "raw/"+uri+"/"+defaultFilename);
	}
}
