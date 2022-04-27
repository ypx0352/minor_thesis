package edu.monash.apkscan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.util.Chain;

public class APIExtractorTransformer extends SceneTransformer
{
	public Set<String> accessedAndroidAPIs = new HashSet<String>();
	public Map<String, Set<String>> api2callers = new HashMap<String, Set<String>>();

	protected void internalBodyTransform(Body b) 
	{
		String callerMethodSig = b.getMethod().getSignature();
		
		PatchingChain<Unit> units = b.getUnits();
		
		for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
		{
			Stmt stmt = (Stmt) unitIter.next();
			
			if (stmt.containsInvokeExpr())
			{
				SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
				String methodSig = sootMethod.getSignature();
				
				accessedAndroidAPIs.add(methodSig);
				CommonUtils.put(api2callers, methodSig, callerMethodSig);
			}
		}
	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) 
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			
			if (sc.getName().startsWith("android.support."))
			{
				continue;
			}
			
			List<SootMethod> methods = sc.getMethods();
			
			for (int i = 0; i < methods.size(); i++)
			{
				SootMethod sm = methods.get(i);
				Body body = null;
				try
				{
					body = sm.retrieveActiveBody();
				}
				catch (Exception ex)
				{
					if (Config.DEBUG)
						System.out.println("[DEBUG] No body for method " + sm.getSignature());
				}
				
				if (null != body)
					internalBodyTransform(body);
			}
		}
		
	}
}
