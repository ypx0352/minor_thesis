package edu.monash.apkscan;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import edu.monash.apkscan.manifest.AXmlAttribute;
import edu.monash.apkscan.manifest.AXmlNode;
import edu.monash.apkscan.manifest.ProcessManifest;

/**
 * This class must be executed after the DexHunter step, 
 * otherwise the AndroidManifest.xml file has not been dumped from the APK file.
 * 
 * @author li.li
 *
 */
public class AndroidManifest 
{
	private String manifestPath;
	
	private int minSdkVersion = -1;
	private int targetSdkVersion = -1;
	private int maxSdkVersion = -1;

	private Set<String> permissions;
	
	public AndroidManifest(String apkName)
	{
		manifestPath = apkName + ".unzip" + File.separator + "AndroidManifest.xml";
		
		try 
		{
			InputStream inputStream = new FileInputStream(manifestPath);
			
			ProcessManifest processManifest = new ProcessManifest(inputStream);
			
			List<AXmlNode> usesSdk = processManifest.getManifest().getChildrenWithTag("uses-sdk");
			permissions = processManifest.getPermissions();
			if (usesSdk != null && ! usesSdk.isEmpty())
			{
				AXmlAttribute<?> attr = usesSdk.get(0).getAttribute("minSdkVersion");
				if (null != attr && attr.getValue() instanceof Integer)
				{
					minSdkVersion = Integer.parseInt("" + attr.getValue());
				}
				
				attr = usesSdk.get(0).getAttribute("targetSdkVersion");
				if (null != attr && attr.getValue() instanceof Integer)
				{
					targetSdkVersion = Integer.parseInt("" + attr.getValue());
				}
				
				attr = usesSdk.get(0).getAttribute("maxSdkVersion");
				if (null != attr && attr.getValue() instanceof Integer)
				{
					maxSdkVersion = Integer.parseInt("" + attr.getValue());
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public int getMinSdkVersion() {
		return minSdkVersion;
	}

	public int getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public int getMaxSdkVersion() {
		return maxSdkVersion;
	}

	public Set<String> getPermissions() {
		return permissions;
	}
}
