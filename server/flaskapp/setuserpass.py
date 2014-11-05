# -*- coding: utf-8 -*-
'''
Usage:

    setuserpass.py [-d] username password

Set a user's username/password, creating it
if it did not already exist.

Specifying -d on the commandline removes the user and in that
case a password is not necessary
'''
import sys
from hashlib import sha1
from werkzeug.security import generate_password_hash
from feeder import db
from feeder.models import get_user
from feeder import gauth


# Print help if required
args = sys.argv[1:]
if len(args) == 0 or '-h' in args:
    exit(__doc__)

# Check delete flag
should_delete = False
if '-d' in args:
    should_delete = True
    args.remove('-d')


# Make sure enough arguments were specified
if not should_delete and len(args) < 2:
    exit("Not enough arguments specified. Print help with -h")
elif should_delete and len(args) < 1:
    exit("No username specified. Print help with -h")

if should_delete:
    username = args[0]
else:
    username, password = args

# Get User
user = get_user(username)

if should_delete:
    db.session.delete(user)
    db.session.commit()
    exit("Removed user {}".format(username))

# Generate a password hash
# Make sure to use a byte string
try:
    bpassword = password.encode('utf-8')
except AttributeError:
    # Already bytestring
    bpassword = password
# Then add the salt used by the android client
androidpassword = sha1(gauth.__ANDROID_SALT__ + bpassword)\
    .hexdigest().lower()

# And finally salt it for real
user.passwordhash = generate_password_hash(androidpassword)
db.session.add(user)
db.session.commit()

exit("User updated")
