import os
import shutil


input_root = 'F:/research/data/apk/'
output_dir = 'F:/research/data/apk_simple_name/'
output_error_dir = 'F:/research/data/apk_simple_name/error/'
finish_count = 0
total_count = len(os.listdir(input_root))
try:
    for filename in os.listdir(input_root):
        simple_filename = filename.split('`')[0] + '.apk'
        shutil.copy(os.path.join(input_root, filename), os.path.join(output_dir, simple_filename))
        finish_count += 1
        print('{:.2%}'.format(finish_count / total_count))
except:
    print(filename + ' error!')
    shutil.copy(os.path.join(input_root, filename), os.path.join(output_error_dir, filename))