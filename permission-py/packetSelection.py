import pandas as pd
from datetime import datetime
import time


start_time = time.time()
df_read = pd.read_csv("androzooopen-original.csv", low_memory=False)
print('read {} rows.'.format(len(df_read)))
file_name_list = []
entry_list = []
date_list = []
# for i in range(0, len(df_read)):
#     try:
#         if df_read.loc[i, "on_googleplay"] == "Yes":
#             date_str = df_read.loc[i, "update_time"].split("T")[0]
#             date_obj = datetime.strptime(date_str, '%Y-%m-%d')
#             release_date_obj = datetime.strptime("2015-10-05", '%Y-%m-%d')
#             if date_obj > release_date_obj:
#                 file_name = df_read.loc[i, "entry"].split("/")[1] + "-master.zip"
#                 file_name_list.append(file_name)
#                 print(file_name + " added.")
#     except Exception:
#         df_read._set_value(i, 'exception', 0)
#         df_read.to_csv('androzooopen-original.csv')
#         print('write exception')

for index, row in df_read.iterrows():
    try:
        if row['on_googleplay'] == 'Yes':
            date_str = row['update_time'].split('T')[0]
            date_obj = datetime.strptime(date_str, '%Y-%m-%d')
            release_date_obj = datetime.strptime("2015-10-05", '%Y-%m-%d')
            if date_obj > release_date_obj:
                file_name = row['entry'].split('/')[1] + "-master.zip"
                file_name_list.append(file_name)
                print(file_name + " added.")
    except Exception:
        entry_list.append(row['entry'])
        date_list.append(row['update_time'])
        print('exception')
structure_result = {"file_name" : file_name_list}
structure_error = {"entry": entry_list, "update_time": date_list}
print('generate final csv file')
pd.DataFrame(structure_result).to_csv('result.csv')
pd.DataFrame(structure_error).to_csv("error.csv")
print('task finish')
print('------ {} minutes'.format((time.time() - start_time) / 60))



