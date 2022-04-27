import pandas as pd
import locate_file_with_specific_string_reuse
import shutil
import os

input_dir = 'F:/research/data/targetSdkVersion_test_result/version23_plus/'
input_file = 'F:/research/analysis/permission_related_string_check_result_update/project_with_sdk23_data_source_permission_related_string_check_result.csv'
output_dir = 'F:/research/analysis/permission_related_string_check_result_update/manual_check_none_method_with_permissions_in_code/'

df_read = pd.read_csv(input_file)
for index, row in df_read.iterrows():
    if not pd.isna(df_read.loc[index, 'dangerous_permissions_handled_source_code']) and row['use_method_count'] == 0:
        filename = row['project_name']
        target_str_list = row['dangerous_permissions_handled_source_code'].split(' ')
        locate_file_with_specific_string_reuse.locate(input_dir, filename, target_str_list)
        shutil.copy2(os.path.join(input_dir, filename), output_dir)