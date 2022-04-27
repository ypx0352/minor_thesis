import model.PermissionEntry;
import soot.*;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AndroidCallGraph {
    private String apkPath;
    private static String androidJar = "C:\\research\\data\\android-platforms-master";
    private String appName;

    public AndroidCallGraph(Path path){
        this.apkPath = path.toAbsolutePath().toString();
        this.appName = path.toFile().getName();
    }

    public static void loopEdge(String appName,String parentName,CallGraph callGraph,Set<PermissionEntry> permissionEntries, Iterator<Edge> it, int level) {
        if (level == 2)
            return;
        for (; it.hasNext(); ) {
            Edge edge = it.next();
            SootMethod method = edge.getTgt().method();
            if(method.hasActiveBody()){
                AndroidUtil.analyseMethodBody(appName,parentName,permissionEntries,method.getActiveBody(),edge.getSrc().method());
            }
            loopEdge(appName,parentName,callGraph,permissionEntries,callGraph.edgesOutOf(edge.getTgt()),level+1);
        }
    }

    public String analyse(){
        // Parse arguments
        InfoflowConfiguration.CallgraphAlgorithm cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.SPARK;

        final InfoflowAndroidConfiguration config = AndroidUtil.getFlowDroidConfig(apkPath, androidJar, cgAlgorithm);
        SetupApplication app = new SetupApplication(config);
        // Create the Callgraph without executing taint analysis
        app.constructCallgraph();
        CallGraph callGraph = Scene.v().getCallGraph();
        // Print some general information of the generated callgraph. Note that although usually the nodes in callgraph
        // are assumed to be methods, the edges in Soot's callgraph is from Unit to SootMethod.
        ManifestUtils manifestUtils = new ManifestUtils(apkPath);
        if(manifestUtils.getTargetSdkVersion()<23)
            return "";
        AndroidCallGraphFilter androidCallGraphFilter = new AndroidCallGraphFilter(manifestUtils.getPackageName());
        Set<PermissionEntry> permissionEntries = new HashSet<>();
        for (SootClass sootClass : androidCallGraphFilter.getValidClasses()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {
                for (Iterator<Edge> it = callGraph.edgesOutOf(sootMethod); it.hasNext(); ) {
                    Edge outEdge = it.next();
                    for (Iterator<Edge> inIt = callGraph.edgesInto(sootMethod);inIt.hasNext();){
                        Edge edge = inIt.next();
                        loopEdge(appName,sootClass.getName(),callGraph,permissionEntries,callGraph.edgesOutOf(edge.getSrc().method()),1);
                    }
                    if (outEdge.getTgt().method().getName().contains("requestPermissions")) {
                        Body methodBody = sootMethod.getActiveBody();
                        AndroidUtil.analyseMethodBody(appName, sootClass.getName(), permissionEntries, methodBody, outEdge.getSrc().method());
                    }
                    loopEdge(appName,sootClass.getName(),callGraph,permissionEntries,callGraph.edgesOutOf(outEdge.getTgt().method()),0);
                }
            }
        }

        return permissionEntries.stream().map(PermissionEntry::toCSVString).collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        System.out.println(new AndroidCallGraph(Paths.get("C:\\research\\data\\overpriviledge_dataset\\NoMansWallpaperApp.apk")).analyse());
    }

    // A Breadth-First Search algorithm to get all reachable methods from initialMethod in the callgraph
    // The output is a map from reachable methods to their parents
    public static Map<SootMethod, SootMethod> getAllReachableMethods(SootMethod initialMethod) {
        CallGraph callgraph = Scene.v().getCallGraph();
        List<SootMethod> queue = new ArrayList<>();
        queue.add(initialMethod);
        Map<SootMethod, SootMethod> parentMap = new HashMap<>();
        parentMap.put(initialMethod, null);
        for (int i = 0; i < queue.size(); i++) {
            SootMethod method = queue.get(i);
            for (Iterator<Edge> it = callgraph.edgesOutOf(method); it.hasNext(); ) {
                Edge edge = it.next();
                SootMethod childMethod = edge.tgt();
                if (parentMap.containsKey(childMethod))
                    continue;
                parentMap.put(childMethod, method);
                queue.add(childMethod);
            }
        }
        return parentMap;
    }

    public static String getPossiblePath(Map<SootMethod, SootMethod> reachableParentMap, SootMethod it) {
        String possiblePath = null;
        while (it != null) {
            String itName = it.getDeclaringClass().getShortName() + "." + it.getName();
            if (possiblePath == null)
                possiblePath = itName;
            else
                possiblePath = itName + " -> " + possiblePath;
            it = reachableParentMap.get(it);
        }
        return possiblePath;
    }
}
