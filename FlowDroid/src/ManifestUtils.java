import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ManifestUtils {

    private int minSdkVersion = -1;
    private int targetSdkVersion = -1;
    private int maxSdkVersion = -1;
    private String packageName;

    private Set<String> permissions;

    public ManifestUtils(String apkPath)
    {


        try
        {

            ProcessManifest processManifest = new ProcessManifest(apkPath);
            packageName = processManifest.getPackageName();

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

    public String getPackageName() {
        return packageName;
    }
}
