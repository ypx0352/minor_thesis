import shutil
from zipfile import ZipFile
import re
import os

manual_check_dir = 'F:/research/data/targetSdkVersion_test_result/manual_check/'
version_23_minus_dir = 'F:/research/data/targetSdkVersion_test_result/version23_minus/'
version_23_plus_dir = 'F:/research/data/targetSdkVersion_test_result/version23_plus/'
without_gradle_file_dir = 'F:/research/data/targetSdkVersion_test_result/without_gradle_file/'
exception_dir = 'F:/research/data/targetSdkVersion_test_result/exception_file/'
input_root = 'F:/research/data/project_dangerous_permission/'
translation = {61: None, 32: None, 9: None, 11: None, 125: None, 40: None, 41: None}
total_count = len(os.listdir(input_root))
finish_count = 0

for file_name in os.listdir(input_root):
    try:
        finish_count += 1
        print('{:.2%} completed...'.format(finish_count / total_count))
        project_path = input_root + file_name
        print('*' * 80)
        print(file_name)
        print(project_path)
        has_gradle = False
        version_is_digital = False
        with ZipFile(project_path, 'r') as zipObj:
            for elem in zipObj.infolist():
                if re.search('build.gradle', elem.filename):
                    has_gradle = True
                    with zipObj.open(elem.filename) as f:
                        content = f.read().decode("utf-8")
                        index = content.find('targetSdkVersion')
                        if index != -1:
                            version = content[index: index + 22].translate(translation).rstrip().split('sion')[1]
                            print('version', repr(version))
                            if version.isdigit():
                                version_is_digital = True
                                if int(version) >= 23:
                                    shutil.copy2(project_path, version_23_plus_dir)
                                    print(f'{file_name} is 23+.')
                                    break
                                else:
                                    shutil.copy2(project_path, version_23_minus_dir)
                                    print(f'{file_name} is 23-.')
                                    break

        if not has_gradle:
            shutil.copy2(project_path, without_gradle_file_dir)
            print(f'{file_name} without gradle file.')
            continue

        if not version_is_digital:
            shutil.copy2(project_path, manual_check_dir)
            print(f'{file_name} needs manual check.')

    except:
        shutil.copy2(project_path, exception_dir)
        print(f'{file_name} is an exception.')
