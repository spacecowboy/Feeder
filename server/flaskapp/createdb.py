# -*- coding: utf-8 -*-
'''
Usage
    python createdb.py -c /path/to/config.yaml

Creates the database whose location is specified by the config file.
See config-sample.yaml for details.
'''
import sys
from feeder import read_config


if __name__ == '__main__':
    if len(sys.argv) != 3 or sys.argv[1] != '-c':
        exit(__doc__)

    configfile = sys.argv[2]
    read_config(configfile)

    import feeder.rest
    from feeder.database import db
    db.create_all()

    exit("Database created successfully")
