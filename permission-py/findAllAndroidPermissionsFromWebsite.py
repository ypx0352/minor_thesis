import requests
from bs4 import BeautifulSoup
import re
import pandas as pd


output_file = "Android_permissions_on_official_website.csv"

def findAllDivs():

    r = requests.get('https://developer.android.com/reference/android/Manifest.permission')
    print(r.status_code)
    soup = BeautifulSoup(r.text, 'html.parser')
    h3s = soup.findAll(attrs={'class': 'api-name'})
    for h3 in h3s:
        try:
            print('*' * 20)
            whole_tag = h3.parent
            permission = h3.string
            print(permission)
            a_tag = whole_tag.find('a')
            api_level = a_tag.string.split(' ')[-1]
            print(api_level)
            protection_level = whole_tag.find(text=re.compile('Protection level:')).split(' ')[-1]
            print(protection_level)
            constant_value = whole_tag.find(text=re.compile('Constant Value:')).split('"')[-2]
            print(constant_value)

        except:
            print('EXCEPTION!!!', permission)

        finally:
            df = pd.DataFrame(
                {
                    'permission': [permission],
                    'protection_level': [protection_level],
                    'constant_value': [constant_value],
                    'added_in_API_level': [api_level]
                }
            )

            df.to_csv(output_file, mode='a', header=False, index=False)
    print('Task finished.')



def extractMessageFromDiv(html):

    print('*' * 20)
    soup2 = BeautifulSoup(html, 'html.parser')
    h3_tag = soup2.find('h3')
    permission = h3_tag.string
    print(permission)
    a_tag = soup2.find('a')
    api_level = a_tag.string.split(' ')[-1]
    print(api_level)
    protection_level = soup2.find(text=re.compile('Protection level:')).split(' ')[-1]
    print(protection_level)
    constant_value = soup2.find(text=re.compile('Constant Value:')).split('"')[-2]
    print(constant_value)

    df = pd.DataFrame(
        {
            'permission': [permission],
            'protection_level': [protection_level],
            'constant_value': [constant_value],
            'added_in_API_level': [api_level]
        }
    )


# df.to_csv(output_file, mode='a', header=False, index=False)
# print('Task finished.')

if __name__ == '__main__':
    findAllDivs()
