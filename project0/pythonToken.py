import requests
import os

# make sure you configure these three variables correctly before you try to run the code 
AZURE_ENDPOINT_URL='https://login.microsoftonline.com/922d5c54-3308-4a57-b2d7-60f0ddc530c3/oauth2/token'
AZURE_APP_ID='49f3e809-7294-4a4e-988f-c10da6b635f5'
AZURE_APP_SECRET='temp'

def get_token_from_client_credentials(endpoint, client_id, client_secret):
    payload = {
        'grant_type': 'client_credentials',
        'client_id': client_id,
        'client_secret': client_secret,
        'resource': 'https://management.core.windows.net/',
    }
    response = requests.post(endpoint, data=payload).json()
    print response
    return response['access_token']

# test
if __name__ == '__main__':
    auth_token = get_token_from_client_credentials(endpoint=AZURE_ENDPOINT_URL,
            client_id=AZURE_APP_ID,
            client_secret=AZURE_APP_SECRET)

    print auth_token
