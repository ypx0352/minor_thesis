package edu.monash.apkscan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ApkScanMain
{
	public static void main(String[] args) 
	{
		Config.apkPath = args[0];
		Config.androidJars = "F:\\research\\data\\android-platforms-master";
		Config.apiPath = "F:\\research\\data\\ApkScan\\ApkScan\\runtime_apis.txt";
		Config.apkName = Config.apkPath;
//
//		String apkName = Config.apkPath;
//		if (apkName.contains("/"))
//		{
//			Config.apkName = apkName.substring(apkName.lastIndexOf('/')+1);
//		}
//
		Set<String> additionalDexes = new DexHunter(Config.apkPath).hunt();
		
		AndroidManifest manifest = new AndroidManifest(Config.apkName);
		int apiLevel = inferAPILevel(manifest);
		int targetSdkVersion = manifest.getTargetSdkVersion();
		System.out.println(targetSdkVersion);
		try(BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\research\\data\\targetSdkVersion.txt"))){
			writer.write(targetSdkVersion);
		}catch (IOException e){
			e.printStackTrace();
		}
		APIExtractor extractor = new APIExtractor();
		extractor.transform(Config.apkPath, Config.androidJars, apiLevel);
		System.out.println("Found " + additionalDexes.size() + " additional DEX files. Now visiting them one by one.");
		for (String dex : additionalDexes)
		{
			extractor.transform(dex, Config.androidJars, apiLevel);
		}

		String permissionResult = String.join("\n", manifest.getPermissions());
		try(BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\research\\data\\user_permissions.txt"))){
			writer.write(permissionResult);
		}catch (IOException e){
			e.printStackTrace();
		}
		
		clean(Config.apkName);
		
		if (Config.DEBUG)
		{
			extractor.usedAndroidAPIs.forEach(api -> System.out.println(api));
		}

		Set<String> tempAndroidAPIs = new HashSet<String>();
		extractor.usedAndroidAPIs.forEach(api -> tempAndroidAPIs.add(extractSimplifiedMethodSig(api)));
		
		Set<String> apis2 = new HashSet<String>();
		for (String api : tempAndroidAPIs)
		{
			apis2.add(new MethodSignature(api).getCompactSignature());
		}

		
		System.out.println("The following APIs are used by this app:");
		apis2.forEach(api -> System.out.println(api));
		try(BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\research\\data\\result.txt"))){
			apis2.forEach(api-> {
				try {
					writer.write(api+'\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	
	
	public static int inferAPILevel(AndroidManifest manifest)
	{
		int apiLevel = -1;
		if (-1 != manifest.getTargetSdkVersion())
		{
			apiLevel = manifest.getTargetSdkVersion();
		}
		else if (-1 != manifest.getMaxSdkVersion())
		{
			apiLevel = manifest.getMaxSdkVersion();
		}
		else
		{
			apiLevel = Config.DEFAULT_API_LEVEL;
		}
		
		return apiLevel;
	}
	
	public static void clean(String apkName)
	{
		try 
		{
			FileUtils.deleteDirectory(new File(apkName + ".unzip"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Get rid of the package name of return type and parameters
	 * <android.app.Activity: void onActivityResult(int,int,android.content.Intent)>
	 * change to
	 * <android.app.Activity: void onActivityResult(int,int,Intent)>
	 * @param api
	 * @return
	 */
	public static String extractSimplifiedMethodSig(String api) {
		if (!(api.startsWith("<") && api.endsWith(">"))) {
			return api;
		}
		StringBuilder sb = new StringBuilder();
		String[] sigArr = api.split(" ");
		sb.append(sigArr[0] + " ");
		sb.append(sigArr[1] + " ");
		int openBracketPos = sigArr[2].indexOf('(');
		int closeBracketPos = sigArr[2].indexOf(')');
		sb.append(sigArr[2].substring(0, openBracketPos + 1));
		String paramStr = sigArr[2].substring(openBracketPos + 1, closeBracketPos);
		String paramSeperator = "";
		if (paramStr.contains(",")) {
			String[] params = paramStr.split(",");
			for (String param : params) {
				sb.append(paramSeperator);
				paramSeperator = ",";
				sb.append(param);
			}
		} else {
			sb.append(paramStr + paramSeperator);
		}
		sb.append(sigArr[2].substring(closeBracketPos));

		return sb.toString();
	}
}