import re
import locate_file_with_specific_string_reuse
# import pandas as pd
#
# def forward(chars):
#     result = []
#     for j in range(0, len(chars)):
#         first_item = chars[0]
#         chars.append(first_item)
#         chars.remove(first_item)
#         print(chars)
#         item = ''
#         for i in range(0, len(chars)):
#             item += chars[i]
#             if item not in result:
#                 result.append(item)
#
#     print(result)
#
#
# def listToString(s):
#     # initialize an empty string
#     str1 = " "
#
#     # return string
#     return (str1.join(s))
#
# if __name__ == '__main__':
#    list = ['a', 'b', 'c']
#    df2 = pd.DataFrame(
#        {
#         "column1": [listToString(list)],
#         "column2": [True],
#         "column3": [111]
#        }
#    )
#    #
#    # df.to_csv('abc.csv', header=['name', 'value'], index=False)
#
#    df2.to_csv('abc.csv', mode='a', header=False, index=False)

locate_file_with_specific_string_reuse.locate('F:/research/data/targetSdkVersion_test_result/version23_plus/'
                                              , 'call_manage-master.zip', ['com.github.quickpermissions', 'com.nabinbhandari.android', 'com.karumi:dexter'])


