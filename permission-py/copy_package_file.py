import os
import shutil
import pandas as pd


filename_source = 'F:/research/scope/has_dangerous_permission_result.csv'
original_root = 'F:/research/data/project_download/'
target_root = 'F:/research/data/project_dangerous_permission/'


error_filename_list = []
error_reason_list = []
data_structure = {"file_name": error_filename_list,
                  "exception": error_reason_list
                  }
if not os.path.exists(target_root):
    os.mkdir(target_root)
try:
    df_read = pd.read_csv(filename_source, low_memory=False)
    entry_count = len(df_read)
    completed_count = 0
    for index, row in df_read.iterrows():
        filename = row['file_name']
        original_path = original_root + filename
        target_path = target_root + filename
        shutil.copyfile(original_path, target_path)
        completed_count += 1
        percentage = '{:.0%}'.format(completed_count / entry_count)
        print('{}  copied. {} completed...'.format(filename, percentage))

except Exception as e:
    error_filename_list.append(filename)
    error_reason_list.append(e)

print('Generate error file...')
pd.DataFrame(data_structure).to_csv(target_root + 'error.csv')
print('Task finished.')
