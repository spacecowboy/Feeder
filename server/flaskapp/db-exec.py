# -*- coding: utf-8 -*-
'''
Usage
    python db-exec.py -c /path/to/config.yaml

Executes SQL against the database whose location is specified by the
config file.
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

    # Use db.engine.execute("")

    db.engine.execute("CREATE INDEX ix_feeds_timestamp ON feeds (timestamp);")
    db.engine.execute("CREATE UNIQUE INDEX ix_feeds_link ON feeds (link);")
    db.engine.execute("CREATE UNIQUE INDEX ix_users_email ON users (email);")
    db.engine.execute("CREATE INDEX ix_userdeletions_timestamp ON userdeletions (timestamp);")
    db.engine.execute("CREATE INDEX ix_feeditems_link ON feeditems (link);")
    db.engine.execute("CREATE INDEX ix_feeditems_timestamp ON feeditems (timestamp);")
