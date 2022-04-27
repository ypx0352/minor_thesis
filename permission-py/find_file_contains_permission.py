import re
import pandas as pd
from zipfile import ZipFile
from bs4  import BeautifulSoup
import shutil

input_txt_path = 'F:/research/analysis/permission_related_string_check_result/project_with_sdk23_data_source/project_name_list.txt'
source_dir = 'F:/research/data/targetSdkVersion_test_result/version23_plus/'
output_dir = 'F:/research/analysis/permission_related_string_check_result/project_with_sdk23_data_source/manual_check_none_method_with_permissions_in_code/'

df_read_dangerous_permission = pd.read_csv('dangerous_permission.csv')
dangerous_permission_list = df_read_dangerous_permission['permission'].values.tolist()

with open(input_txt_path, 'r') as f:
    project_names = f.readlines()

for project_name in project_names:
    print('*'*15)
    print(project_name.rstrip())
    project_path = source_dir + project_name.rstrip()
    dangerous_permissions_in_manifest = []
    dangerous_permissions_in_source_code = []
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
                with zipObj.open(filename) as f:
                    content = f.read().decode('utf-8')
                    for permission in dangerous_permissions_in_manifest:
                        index = content.find(permission)
                        if index != -1:
                            print(filename)
    shutil.copy2(project_path, output_dir)