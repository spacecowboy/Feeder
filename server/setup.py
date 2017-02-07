#!/usr/bin/env python

from distutils.core import setup
from build_manpage import BuildManPage
from feeder.version import __version__


setup(name='feeder',
      version=__version__,
      description='Caching RSS/Atom server',
      author='Jonas Kalderstam',
      author_email='jonas@cowboyprogrammer.org',
      url='https://github.com/spacecowboy/feeder',
      packages=['feeder'],
      scripts=['scripts/feeder'],
      cmdclass={'build_manpage': BuildManPage}
     )
