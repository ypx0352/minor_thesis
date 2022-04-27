from zipfile import ZipFile
import re
import os
from bs4 import BeautifulSoup
import pandas as pd
import shutil


def listToString(list):
    # initialize an empty string
    str1 = " "
    # return string
    return str1.join(list)


df_read_dangerous_permission = pd.read_csv('dangerous_permission.csv')
dangerous_permission_list = df_read_dangerous_permission['permission'].values.tolist()
test_methods = ['onRequestPermissionsResult', 'checkSelfPermission', 'requestPermissions', 'shouldShowRequestPermissionRationale']
input_root = 'H:/research/data/targetSdkVersion_test_result/version23_plus/'
exception_dir = 'H:/research/data/permission_related_string_check_result/project_with_sdk23_data_source/exception_project/'
result_path = 'H:/research/data/permission_related_string_check_result/project_with_sdk23_data_source/permission_related_string_check_result.csv'
total_count = len(os.listdir(input_root))
finish_count = 0
for project_name in os.listdir(input_root):
    try:
        finish_count += 1
        print('{:.2%} finished...'.format(finish_count / total_count))
        use_methods = []
        dangerous_permissions_in_manifest = []
        dangerous_permissions_in_source_code = []
        has_java_file = False
        print('*' * 15)
        print(project_name)
        project_path = os.path.join(input_root, project_name)
        print(project_path)
        with ZipFile(project_path, 'r') as zipObj:
            for filename in zipObj.namelist():
                if re.search('AndroidManifest.xml', filename):
                    with zipObj.open(filename, 'r') as f:
                        soup = BeautifulSoup(f, 'html.parser')
                        tags = soup.findAll('uses-permission')
                        for tag in tags:
                            permission = tag['android:name']
                            if permission in dangerous_permission_list:
                                dangerous_permissions_in_manifest.append(permission.split('.')[-1])

                if filename[-5:] == '.java':
                    has_java_file = True
                    with zipObj.open(filename) as f:
                        content = f.read().decode('utf-8')
                        for method in test_methods:
                            index = content.find(method)
                            if index != -1:
                                if method not in use_methods:
                                    use_methods.append(method)
                        for permission in dangerous_permissions_in_manifest:
                            index = content.find(permission)
                            if index != -1:
                                if permission not in dangerous_permissions_in_source_code:
                                    dangerous_permissions_in_source_code.append(permission)
        print('use methods: ', use_methods)
        print('permissions in manifest: ', dangerous_permissions_in_manifest)
        print('permissions in code: ', dangerous_permissions_in_source_code)
        if has_java_file:
            language = 'Java'
        else:
            language = 'Kotlin'

        if len(dangerous_permissions_in_manifest) == len(dangerous_permissions_in_source_code):
            matched = '='
        elif len(dangerous_permissions_in_manifest) > len(dangerous_permissions_in_source_code):
            matched = '>'
        else:
            matched = '<'

        df = pd.DataFrame(
            {
                'project_name': [project_name],
                'dangerous_permissions_declared_manifest': [listToString(dangerous_permissions_in_manifest)],
                'are_dangerous_permissions_matched': [matched],
                'dangerous_permissions_handled_source_code': [listToString(dangerous_permissions_in_source_code)],
                'onRequestPermissionsResult': ['onRequestPermissionsResult' in use_methods],
                'checkSelfPermission': ['checkSelfPermission' in use_methods],
                'requestPermissions': ['requestPermissions' in use_methods],
                'shouldShowRequestPermissionRationale': ['shouldShowRequestPermissionRationale' in use_methods],
                'use_method_count': [len(use_methods)],
                'language': [language]
            }
        )
        df.to_csv(result_path, mode='a', header=False, index=False)
    except Exception as e:
        print('Exception!!!')
        print(e)
        shutil.copy2(project_path, exception_dir)


print('Task finished.')

