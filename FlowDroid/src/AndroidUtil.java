import model.PermissionEntry;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class AndroidUtil {
    private static final List<String> dangerousPermissionList = Arrays.asList("android.permission.ACCEPT_HANDOVER", "android.permission.ACCESS_BACKGROUND_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_MEDIA_LOCATION",
            "android.permission.ACTIVITY_RECOGNITION",
            "com.android.voicemail.permission.ADD_VOICEMAIL",
            "android.permission.ANSWER_PHONE_CALLS",
            "android.permission.BODY_SENSORS",
            "android.permission.CALL_PHONE",
            "android.permission.CAMERA",
            "android.permission.GET_ACCOUNTS",
            "android.permission.PROCESS_OUTGOING_CALLS",
            "android.permission.READ_CALENDAR",
            "android.permission.READ_CALL_LOG",
            "android.permission.READ_CONTACTS",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.READ_PHONE_NUMBERS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_MMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.RECEIVE_WAP_PUSH",
            "android.permission.RECORD_AUDIO",
            "android.permission.SEND_SMS",
            "android.permission.USE_SIP",
            "android.permission.WRITE_CALENDAR",
            "android.permission.WRITE_CALL_LOG",
            "android.permission.WRITE_CONTACTS",
            "android.permission.WRITE_EXTERNAL_STORAGE");

    public static String getPackageName(String apkPath) {
        String packageName = "";
        try {
            ProcessManifest manifest = new ProcessManifest(apkPath);
            packageName = manifest.getPackageName();
            System.out.println(manifest.getManifest().getAttribute("targetSdkVersion").getValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return packageName;
    }

    public static boolean isAndroidMethod(SootMethod sootMethod) {
        String clsSig = sootMethod.getDeclaringClass().getName();
        List<String> androidPrefixPkgNames = Arrays.asList("android.", "com.google.android", "androidx.");
        return androidPrefixPkgNames.stream().map(clsSig::startsWith).reduce(false, (res, curr) -> res || curr);
    }

    public static InfoflowAndroidConfiguration getFlowDroidConfig(String apkPath, String androidJar) {
        return getFlowDroidConfig(apkPath, androidJar, InfoflowConfiguration.CallgraphAlgorithm.SPARK);
    }

    public static InfoflowAndroidConfiguration getFlowDroidConfig(String apkPath, String androidJar, InfoflowConfiguration.CallgraphAlgorithm cgAlgorithm) {
        final InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
        config.getAnalysisFileConfig().setTargetAPKFile(apkPath);
        config.getAnalysisFileConfig().setAndroidPlatformDir(androidJar);
        config.setMaxThreadNum(32);
//        config.getCallbackConfig().setMaxAnalysisCallbackDepth(8);
        config.setCodeEliminationMode(InfoflowConfiguration.CodeEliminationMode.NoCodeElimination);
        config.setEnableReflection(true);
        config.setCallgraphAlgorithm(cgAlgorithm);
        return config;
    }

    public static List<String> getDangerousPermissionList() {
        return dangerousPermissionList;
    }

    public static Set<String> getPermissionsInArray(String permissionArrayParameter, PatchingChain<Unit> units) {
        //查找string数组的权限名字
        Set<String> resultSet = new HashSet<>();
        if (!permissionArrayParameter.isEmpty()) {
            for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); ) {
                Stmt stmt = (Stmt) unitIter.next();
                //一条语句 $r1 = "xxxxx" 的size为3，包括左边的r1，中间的 = 和右边的"xxxx"
                //于是判断位置0是不是string[]的variable，然后读取位置2的值
                List<ValueBox> valueBoxes = stmt.getUseBoxes();
                int size = stmt.getUseBoxes().size();
                if (size == 3 && valueBoxes.get(0).getValue().toString().contains(permissionArrayParameter) || (valueBoxes.size()>2 && valueBoxes.get(2).getValue().toString().contains("android.permission"))) {
                    //System.out.println(valueBoxes.get(0).getValue().toString() + ": " + valueBoxes.get(2).getValue().toString());
                    String permission = valueBoxes.get(2).getValue().toString().replace("\"", "");
                    if (dangerousPermissionList.contains(permission)) {
                        resultSet.add(valueBoxes.get(2).getValue().toString());
                    }
                }
            }
        } else {
            return new HashSet<>();
        }
        return resultSet;
    }

    public static void analyseMethodBody(String appName, String parentEdgeName, Set<PermissionEntry> permissionEntryMap, Body b, SootMethod method) {
        PatchingChain<Unit> units = b.getUnits();
        String callerMethodSig = b.getMethod().getSignature();
        String permissionArrayStringName = "";
        Set<String> permissionsChecked = new HashSet<>();
        for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); ) {
            Stmt stmt = (Stmt) unitIter.next();

            //这里过滤所有赋值语句，找到每行内容是调用方法的代码。
            if (stmt.containsInvokeExpr()) {
                //得到这个调用方法语句的信息
                SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
                if (sootMethod.getSignature().contains("checkSelfPermission")) {

                    //List<Value> args = stmt.getInvokeExpr().getArgs();
                    for (Value value : stmt.getInvokeExpr().getArgs()) {
                        if (value.getType().toQuotedString().contains("java.lang.String")) {
                            Consumer<String> addCheck = (name)->{
                                final PermissionEntry entry;
                                final PermissionEntry temp = new PermissionEntry(appName, parentEdgeName, name);
                                if (!permissionEntryMap.contains(temp)) {
                                    entry = new PermissionEntry(appName, parentEdgeName, name);
                                    permissionEntryMap.add(entry);
                                } else {
                                    entry = permissionEntryMap.stream().filter(permissionEntry -> permissionEntry.equals(temp)).findFirst().get();
                                }
                                if(entry.isRequested()){
                                    entry.setCheckBeforeRequest(true);
                                }
                                entry.setChecked(true);
                                entry.setCheckedCaller(callerMethodSig);
                            };
                            if(value.toString().contains("permission")){
                                addCheck.accept(value.toString());
                            }else{
                               Set<String> permission = getPermissionsInArray(value.toString(),units);
                               if(permission.size()>0){
                                   permission.forEach(addCheck);
                               }else{
                                   permission = getPermissionsInArray(value.toString(),method.getActiveBody().getUnits());
                                   permission.forEach(addCheck);
                               }
                            }
                            System.out.println("checked: " + permissionsChecked);
                        }
                    }
                }

                if (sootMethod.getSignature().contains("requestPermission")) {
                    //System.out.println(b);
                    //System.out.println("caller: " + callerMethodSig + " method name: " + sootMethod.getSignature());
                    //查找调用方法的参数内容，如果是String[] 则认为这个是权限名的数组
                    for (Value value : stmt.getInvokeExpr().getArgs()) {
                        if (value.getType().toQuotedString().contains("java.lang.String[]")) {
                            permissionArrayStringName = value.toString();
                            for (String permissionRequested : getPermissionsInArray(permissionArrayStringName, units)) {
                                final PermissionEntry temp = new PermissionEntry(appName, parentEdgeName, permissionRequested);
                                final PermissionEntry entry;
                                if (permissionEntryMap.contains(temp)) {
                                    entry = permissionEntryMap.stream().filter(permissionEntry -> permissionEntry.equals(temp)).findFirst().get();
                                } else {
                                    entry = new PermissionEntry(appName, parentEdgeName, permissionRequested);
                                    permissionEntryMap.add(entry);
                                }
                                entry.setRequested(true);
                                entry.setRequestCaller(callerMethodSig);
                                if (entry.isChecked()) {
                                    entry.setCheckBeforeRequest(true);
                                }
                            }
                        }
                    }
                }
                if (sootMethod.getSignature().contains("shouldShowRequestPermissionRationale")) {
                    for (Value value : stmt.getInvokeExpr().getArgs()) {
                        if (value.getType().toQuotedString().contains("java.lang.String")) {

                            Consumer<String> addShould = (name)->{
                                final PermissionEntry entry;
                                final PermissionEntry temp = new PermissionEntry(appName, parentEdgeName, name);
                                if (!permissionEntryMap.contains(temp)) {
                                    entry = new PermissionEntry(appName, parentEdgeName, name);
                                    permissionEntryMap.add(entry);
                                } else {
                                    entry = permissionEntryMap.stream().filter(permissionEntry -> permissionEntry.equals(temp)).findFirst().get();
                                }
                                entry.setHasShouldShowRational(true);
                                entry.setShouldShowRationalCaller(callerMethodSig);
                            };

                            if(value.toString().contains("permission")){
                                addShould.accept(value.toString());
                            }else{
                                Set<String> permission = getPermissionsInArray(value.toString(),units);
                                if(permission.size()>0){
                                    permission.forEach(addShould);
                                }else{
                                    permission = getPermissionsInArray(value.toString(),method.getActiveBody().getUnits());
                                    permission.forEach(addShould);
                                }
                            }
                        }
                    }
                }
                String methodSig = sootMethod.getSignature();

            }
        }

    }
}