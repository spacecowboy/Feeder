#!/usr/bin/env python

from distutils.core import setup

setup(name='feeder',
      version='1.0.0',
      description='Caching RSS/Atom server',
      author='Jonas Kalderstam',
      author_email='jonas@cowboyprogrammer.org',
      url='https://github.com/spacecowboy/feeder',
      packages=['feeder'],
      scripts=['scripts/feeder']
     )
