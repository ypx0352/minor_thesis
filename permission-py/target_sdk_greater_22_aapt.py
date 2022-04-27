from subprocess import run, PIPE
import os


input_root = 'F:/research/data/apk/'
for file_name in os.listdir(input_root):
    apk_path = input_root + file_name
    print(file_name)
    cmd = '''aapt dump badging "{}" | findstr targetSdkVersion'''.format(apk_path)
    result = run(cmd, shell=True, stdout=PIPE, stderr=PIPE, universal_newlines=True)
    print(result)
    #print(result.stdout.split('\n')[0].split(':')[1].strip('\n').strip("'").strip('"'))
    #print(int(result.stdout.split(':')[1].strip('\n').strip("'").strip('"')))

