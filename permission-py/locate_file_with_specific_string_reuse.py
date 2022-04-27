import os
from zipfile import ZipFile



def locate(input_root, filename, target_str_list):
    project_path = os.path.join(input_root, filename)
    print('*' * 15)
    print(filename, target_str_list)
    with ZipFile(project_path, 'r') as zipObj:
        for filename in zipObj.namelist():
            if filename[-5:] == '.java' or filename[-3:] == '.kt' or filename[-5:] == '.dart':
                with zipObj.open(filename) as f:
                    content = f.read().decode('utf-8')
                    for target_str in target_str_list:
                        index = content.find(target_str)
                        if index != -1:
                            print(target_str, filename)

