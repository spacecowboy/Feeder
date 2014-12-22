# -*- coding: utf-8 -*-
'''
Usage:

    hashedpassword.py somesecretpassword1 somesecretpassword2

Prints the hashed version of the passwords given, one per line.
Note that whitespace is stripped.

Options:
  -h : Prints this text
'''
import sys
from hashlib import sha1
from werkzeug.security import generate_password_hash
from feeder import gauth


# Print help if required
args = sys.argv[1:]
if len(args) == 0 or '-h' in args:
    exit(__doc__)

for pw in args:
    bpassword = pw.strip().encode('utf-8')
    # Then add the salt used by the android client
    androidpassword = sha1(gauth.__ANDROID_SALT__ + bpassword)\
        .hexdigest().lower()

    # And finally salt it for real
    print(generate_password_hash(androidpassword))
