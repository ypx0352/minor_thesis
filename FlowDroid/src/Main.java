import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private final static String GOOGLE_BENIGN = "D:\\Documents\\apk\\google_benign";
    private final static String MALWARE = "D:\\Documents\\apk\\malware16171819";
    private final static String result_name_malware = "result_permission_malware.csv";
    private final static String result_name_benigh = "result_permission_benign.csv";
    private final static String DATA_SET = "C:\\research\\data\\overpriviledge_dataset";

    public static void main(String[] args) {
        try{
            List<Path> benign = Files.walk(Paths.get(GOOGLE_BENIGN)).filter(Files::isRegularFile).collect(Collectors.toList());
            List<Path> malware = Files.walk(Paths.get(MALWARE)).filter(Files::isRegularFile).collect(Collectors.toList());
            writeFile(result_name_benigh,benign);
            writeFile(result_name_malware,malware);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void writeFile(String resultName, List<Path> files){
        try (FileWriter writer = new FileWriter(resultName, true)) {
//            try (Stream<Path> paths = Files.walk(Paths.get(GOOGLE_BENIGN))) {
//            List<Path> files = allFiles.stream().filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path file : files) {
                System.out.println("Handle: " + file.toFile().getName());
                String maniFestInfo = analyseManifest(file);
                if (maniFestInfo.isEmpty()) {
                    continue;
                }
                writer.write(maniFestInfo);
                writer.flush();
//                    Thread thread = new Thread(()->{
//                        String result = analyseFile(file);
//                        if(result.trim().isEmpty()){
//                            return;
//                        }
//                        try{
//                            writer.write(result);
//                            writer.flush();
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                    });
//                    thread.start();
//                    for(int i=0;i<=60&&thread.isAlive();i++){
//                        Thread.sleep(1000);
//                        if(i==60){
//                            thread.stop();
//                        }
//                    }

            }

//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String analyseFile(Path file) {
        try {
            return new AndroidCallGraph(file).analyse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String analyseManifest(Path path) {
        try {
            ManifestUtils manifestUtils = new ManifestUtils(path.toAbsolutePath().toString());
            if (manifestUtils.getTargetSdkVersion() < 23)
                return "";
            return manifestUtils.getPackageName() + "," + String.join(";", manifestUtils.getPermissions());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
