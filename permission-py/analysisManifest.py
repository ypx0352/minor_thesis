import pandas as pd
from bs4 import BeautifulSoup
from zipfile import ZipFile
import re
import time

start_time = time.time()
res_list = []
error_list = []
in_root_path = 'F:/project_download_Copy/'

df_read_filename = pd.read_csv('result.csv')
df_read_dangerous_permission = pd.read_csv('dangerous_permission.csv')
dangerous_permission_list = df_read_dangerous_permission['permission'].values.tolist()


for index, row in df_read_filename.iterrows():
    try:
        file_name = row['file_name']
        file_path = in_root_path + file_name
        permission_list = []
        with ZipFile(file_path, 'r') as zipObj:
            for elem in zipObj.infolist():
                if re.search('AndroidManifest.xml', elem.filename):
                    with zipObj.open(elem.filename) as f:
                        soup = BeautifulSoup(f, 'html.parser')
                        tags = soup.findAll('uses-permission')
                        for tag in tags:
                            permission = tag['android:name']
                            if permission in dangerous_permission_list:
                                res_list.append(file_name)
                                print(file_name + " added")
                                break
                    break
    except Exception:
        error_list.append(file_name)
        print(file_name + " error.")

structure_result = {"file_name" : res_list}
structure_error = {"file_name": error_list}
print('generate final csv file')
pd.DataFrame(structure_result).to_csv('has_dangerous_permission_result.csv')
pd.DataFrame(structure_error).to_csv("has_dangerous_permission_error.csv")
print('Task finish.')
print('------ {} minutes'.format((time.time() - start_time) / 60))