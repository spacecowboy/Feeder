import os
from yaml import load
from feeder import app


def read_config(filename):
    configfile = load_yaml(filename)
    for k, v in configfile.items():
        if k == 'FEEDER_DATABASE':
            v = os.path.abspath(v)
            if v.startswith('/'):
                uri = 'sqlite:///' + v
            else:
                uri = 'sqlite:////' + v
            app.config['SQLALCHEMY_DATABASE_URI'] = uri

        if k.isupper():
            # Uppercase to flask
            app.config[k] = v
    # Return configfile to interested parties
    return configfile


def load_yaml(filename):
    '''Read a YAML file'''
    with open(filename, 'r') as FILE:
        return load("".join(FILE.readlines()))
