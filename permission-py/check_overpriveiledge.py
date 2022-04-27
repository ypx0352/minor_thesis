import os
import pandas as pd
import shutil


def listToString(list):
    # initialize an empty string
    str1 = " "
    # return string
    return str1.join(list)

apk_root = r'F:\research\data\apk_simple_name_test'
package_skd23_dir = 'F:/research/data/targetSdkVersion_test_result/version23_plus/'
apk_name_list = os.listdir(apk_root)
result_txt = r'F:\research\data\result.txt'
fake_result_text = r'F:\research\data\fake_result.txt'
fake_permission_txt = r'F:\research\data\fake_user_permissions.txt'
permission_txt = r'F:\research\data\user_permissions.txt'
#targetSdkVersion_text = 'F:/research/data/targetSdkVersion.txt'
result_path = 'F:/research/analysis/overpriviledge.csv'
error_dir = 'F:/research/analysis/overpriviledge_error/'
df_read_dangerous_permission = pd.read_csv('dangerous_permission.csv')
dangerous_permission_list = df_read_dangerous_permission['permission'].values.tolist()
with open('permission_api_mapping.txt') as f:
    mapping_list = f.read().split('\n')


total_count = len(os.listdir(package_skd23_dir))
finish_count = 0
try:
    for apk_package_name in os.listdir(package_skd23_dir):
        each_mapping_list = {}
        overpriviledge_list = []
        targetSdkVersion = ''
        if os.path.exists(result_txt):
            os.remove(result_txt)
        if os.path.exists(permission_txt):
            os.remove(permission_txt)
        # if os.path.exists(targetSdkVersion_text):
        #     os.remove(targetSdkVersion_text)
        index = apk_package_name.find('-master')
        apk_name = apk_package_name[:index] + '.apk'
        if apk_name in apk_name_list:
            apk_path = os.path.join(apk_root, apk_name)
            print(apk_path)
            os.system(r'D:\IT\JavaJDK\bin\java.exe "-javaagent:D:\IT\IntelliJ IDEA 2020.3.3\lib\idea_rt.jar=54890:D:\IT\IntelliJ IDEA 2020.3.3\bin" -Dfile.encoding=UTF-8 -classpath F:\research\data\ApkScan\ApkScan\bin;D:\IT\JavaJDK\jre\lib\charsets.jar;D:\IT\JavaJDK\jre\lib\deploy.jar;D:\IT\JavaJDK\jre\lib\ext\access-bridge-64.jar;D:\IT\JavaJDK\jre\lib\ext\cldrdata.jar;D:\IT\JavaJDK\jre\lib\ext\dnsns.jar;D:\IT\JavaJDK\jre\lib\ext\jaccess.jar;D:\IT\JavaJDK\jre\lib\ext\jfxrt.jar;D:\IT\JavaJDK\jre\lib\ext\localedata.jar;D:\IT\JavaJDK\jre\lib\ext\nashorn.jar;D:\IT\JavaJDK\jre\lib\ext\sunec.jar;D:\IT\JavaJDK\jre\lib\ext\sunjce_provider.jar;D:\IT\JavaJDK\jre\lib\ext\sunmscapi.jar;D:\IT\JavaJDK\jre\lib\ext\sunpkcs11.jar;D:\IT\JavaJDK\jre\lib\ext\zipfs.jar;D:\IT\JavaJDK\jre\lib\javaws.jar;D:\IT\JavaJDK\jre\lib\jce.jar;D:\IT\JavaJDK\jre\lib\jfr.jar;D:\IT\JavaJDK\jre\lib\jfxswt.jar;D:\IT\JavaJDK\jre\lib\jsse.jar;D:\IT\JavaJDK\jre\lib\management-agent.jar;D:\IT\JavaJDK\jre\lib\plugin.jar;D:\IT\JavaJDK\jre\lib\resources.jar;D:\IT\JavaJDK\jre\lib\rt.jar;F:\research\data\ApkScan\ApkScan\libs\axml-2.0.jar;F:\research\data\ApkScan\ApkScan\libs\AXMLPrinter2.jar;F:\research\data\ApkScan\ApkScan\libs\soot-4.1.0-jar-with-dependencies.jar edu.monash.apkscan.ApkScanMain ' + apk_path)
            if os.path.exists(result_txt) and os.path.exists(permission_txt):
                # if os.path.exists(targetSdkVersion_text):
                #     with open(targetSdkVersion_text) as f:
                #         targetSdkVersion = f.readline()
                with open(fake_result_text) as f:
                    apis = f.read().split('\n')

                with open(fake_permission_txt) as f:
                    # permission app has
                    left_list = []
                    right_list = []

                    for row in f.read().split('\n'):
                        if row in dangerous_permission_list:
                            left_list.append(row)
                            right_list.append(row)

                            # all methods of this permission
                            for mapping in mapping_list:
                                if row in mapping.split('::')[-1].strip():
                                    if mapping.split('(')[0] in apis:
                                        right_list.remove(row)
                                        break
                                    #each_mapping_list[mapping.split('(')[0]] = row


                # with open(result_txt) as f:
                #     for row in f.readlines():
                #         if row in each_mapping_list.keys():
                #             overpriviledge_list.remove(each_mapping_list[row])

                print('left', left_list)
                print('right', right_list)

                left_list_length = len(left_list)
                right_list_length = len(right_list)
                if left_list_length > right_list_length:
                    compare = '>'
                elif left_list_length == right_list_length:
                    compare = '='
                else:
                    compare = '<'

                df = pd.DataFrame(
                    {
                        'apk_name': [apk_name],
                        #'targetSdkVersion': [targetSdkVersion],
                        'dangerous_permission_in_manifest': [listToString(left_list)],
                        'compare': [compare],
                        'dangerous_permission_pverpriviledged': [listToString(right_list)]

                    })
                df.to_csv(result_path, mode='a', header=False, index=False)
            else:
                shutil.copy2(apk_path, error_dir)

            finish_count += 1
            print('{:.2%} finished...'.format(finish_count / total_count))
except:
    shutil.copy2(apk_path, error_dir)
print('finish')