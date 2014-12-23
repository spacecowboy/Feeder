from yaml import load
from hashlib import sha1
from werkzeug.security import check_password_hash, generate_password_hash
from feeder import gauth


def read_config(filename):
    with open(filename, 'r') as FILE:
        return load("".join(FILE.readlines()))


if __name__ == '__main__':
    conf = read_config('config-sample.yaml')

    users = conf.get('FEEDER_USERS', {})
    print(users)

    # Password to try with
    password = 'secretpassword'
    # Need to use android hash first
    bpassword = password.strip().encode('utf-8')
    # Then add the salt used by the android client
    password = sha1(gauth.__ANDROID_SALT__ + bpassword)\
        .hexdigest().lower()

    username = 'bob'
    print(username, password)

    # Like in code
    if username not in users:
        # User does not exist
        exit("User does not exist")

    valid = False
    # Get password
    userpass = users[username]
    # Stored hashed
    if userpass.startswith('pbkdf2:sha1'):
        'Starts with, check hash'
        valid = check_password_hash(userpass, password)
    else:
        'Plain, generate first'
        # Generate hash if stored in plaintext
        userpasshash = sha1(gauth.__ANDROID_SALT__ + userpass.encode('utf-8')).hexdigest().lower()
        #userpasshash = generate_password_hash(userpasshash)
        #valid = check_password_hash(userpasshash, password)
        valid = userpasshash == password

    if valid:
        print("valid")
    else:
        print("Not valid")
