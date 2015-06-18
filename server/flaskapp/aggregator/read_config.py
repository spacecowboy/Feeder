# -*- coding: utf-8 -*-
from yaml import load
from aggregator import app


def read_config(filename):
    configfile = load_yaml(filename)
    for k, v in configfile.items():
        if k.isupper():
            # Uppercase to flask
            app.config[k] = v
    # Return configfile to interested parties
    return configfile


def load_yaml(filename):
    '''Read a YAML file'''
    with open(filename, 'r') as FILE:
        return load("".join(FILE.readlines()))
