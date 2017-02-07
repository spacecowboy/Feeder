# -*- coding: utf-8 -*-

# From: https://github.com/pwman3/pwman3/blob/d718a01fa8038893e42416b59cdfcda3935fe878/build_manpage.py
# This file is distributed under the same License of Python
# Copyright (c) 2014 Oz Nahum Tiram  <nahumoz@gmail.com>

"""
build_manpage.py

Add a `build_manpage` command  to your setup.py.
To use this Command class import the class to your setup.py,
and add a command to call this class::

    from build_manpage import BuildManPage

    ...
    ...

    setup(
    ...
    ...
    cmdclass={
        'build_manpage': BuildManPage,
    )

You can then use the following setup command to produce a man page::

    $ python setup.py build_manpage --output=/output/directory

Alternatively, set the variable AUTO_BUILD to True, and just invoke::

    $ python setup.py build

If automatically want to build the man page every time you invoke your build,
add to your ```setup.cfg``` the following::

    [build_manpage]
    output = <appname>.1
    parser = <path_to_your_parser>
"""


import os
import datetime
from distutils.core import Command
from distutils.errors import DistutilsOptionError
from distutils.command.build import build
import argparse
import re as _re

from feeder.cli import get_parsers

AUTO_BUILD = False

class BuildManPage(Command):

    description = 'Generate man page from an ArgumentParser instance.'

    user_options = [
        ('output=', 'O', 'output directory'),
        #('parser=', None, 'module path to an ArgumentParser instance'
        # '(e.g. mymod:func, where func is a method or function which return'
        # 'an arparse.ArgumentParser instance.'),
    ]

    def initialize_options(self):
        self.output = None
        self.parsers = get_parsers(formatter_class=ManPageFormatter)

    def finalize_options(self):
        if self.output is None:
            raise DistutilsOptionError('\'output\' option is required')
        #if self.parser is None:
        #    raise DistutilsOptionError('\'parser\' option is required')
        #mod_name, func_name = self.parser.split(':')
        #fromlist = mod_name.split('.')
        #try:
        #    mod = __import__(mod_name, fromlist=fromlist)
        #    self._parser = getattr(mod, func_name)(formatter_class=ManPageFormatter)
        #except ImportError as err:
        #    raise err

        self.announce('Writing man page(s) to {}'.format(self.output))
        self._today = datetime.date.today()

    def run(self):
        dist = self.distribution
        homepage = dist.get_url()
        author = dist.get_author()
        email = dist.get_author_email()

        manpages = [p.prog.replace(" ", "-") for p in self.parsers]

        for manpage, parser in zip(manpages, self.parsers):
            appname = parser.prog

            sections = {'authors': ("{} <{}>".format(author, email)),
                        'distribution': ("The latest version of {} may be "
                                         "downloaded from {}".format(appname,
                                                                     homepage))
                    }

            dist = self.distribution

            others = manpages.copy()
            others.remove(manpage)

            mpf = ManPageFormatter(appname,
                               desc=dist.get_description(),
                               long_desc=parser.description,
                               ext_sections=sections)

            m = mpf.format_man_page(parser, manpage, see_also=others)

            fname = manpage + ".1"

            with open(os.path.join(self.output, fname), 'w') as f:
                f.write(m)


class ManPageFormatter(argparse.HelpFormatter):
    """
    Formatter class to create man pages.
    This class relies only on the parser, and not distutils.
    The following shows a scenario for usage::

        from pwman import parser_options
        from build_manpage import ManPageFormatter

        # example usage ...

        dist = distribution
        mpf = ManPageFormatter(appname,
                               desc=dist.get_description(),
                               long_desc=dist.get_long_description(),
                               ext_sections=sections)

        # parser is an ArgumentParser instance
        m = mpf.format_man_page(parsr)

        with open(self.output, 'w') as f:
            f.write(m)

    The last line would print all the options and help infomation wrapped with
    man page macros where needed.
    """

    def __init__(self,
                 prog,
                 indent_increment=2,
                 max_help_position=24,
                 width=None,
                 section=1,
                 desc=None,
                 long_desc=None,
                 ext_sections=None,
                 authors=None
                 ):

        super(ManPageFormatter, self).__init__(prog)

        self._prog = prog
        self._section = 1
        self._today = datetime.date.today().strftime('%Y\\-%m\\-%d')
        self._desc = desc
        self._long_desc = long_desc
        self._ext_sections = ext_sections

    def _get_formatter(self, **kwargs):
        return self.formatter_class(prog=self.prog, **kwargs)

    def _markup(self, txt):
        return txt.replace('-', '\\-')

    def _underline(self, string):
        return "\\fI\\s-1" + string + "\\s0\\fR"

    def _bold(self, string):
        if not string.strip().startswith('\\fB'):
            string = '\\fB' + string
        if not string.strip().endswith('\\fR'):
            string = string + '\\fR'
        return string

    def _mk_synopsis(self, parser):
        self.add_usage(parser.usage, parser._actions,
                       parser._mutually_exclusive_groups, prefix='')
        usage = self._format_usage(None, parser._actions,
                                   parser._mutually_exclusive_groups, '')

        usage = usage.replace('%s ' % self._prog, '')
        usage = '.SH SYNOPSIS\n \\fB%s\\fR %s\n' % (self._markup(self._prog),
                                                    usage)
        return usage

    def _mk_title(self, prog):
        return '.TH {0} {1} {2}\n'.format(prog, self._section,
                                          self._today)

    def _make_name(self, name, description):
        """
        this method is in consitent with others ... it relies on
        distribution
        """
        return '.SH NAME\n%s \\- %s\n' % (name,
                                          description)

    def _mk_description(self):
        if self._long_desc:
            long_desc = self._long_desc.replace('\n', '\n.br\n')
            return '.SH DESCRIPTION\n%s\n' % self._markup(long_desc)
        else:
            return ''

    def _mk_see_also(self, commands):
        if commands:
            commands = [self._bold(cmd) for cmd in commands]
            commands = [cmd + "(1)" for cmd in commands]
            cmdline = " ".join(commands)
            long_desc = self._long_desc.replace('\n', '\n.br\n')
            return '.SH SEE ALSO\n{}\n'.format(self._markup(cmdline))
        else:
            return ''

    def _mk_footer(self, sections):
        if not hasattr(sections, '__iter__'):
            return ''

        footer = []
        for section, value in sections.items():
            part = ".SH {}\n {}".format(section.upper(), value)
            footer.append(part)

        return '\n'.join(footer)

    def format_man_page(self, parser, manpage, see_also=None):
        page = []
        page.append(self._mk_title(manpage))
        page.append(self._make_name(manpage, parser.description))
        page.append(self._mk_synopsis(parser))
        page.append(self._mk_description())
        page.append(self._mk_options(parser))
        page.append(self._mk_see_also(see_also))
        page.append(self._mk_footer(self._ext_sections))

        return ''.join(page)

    def _mk_options(self, parser):

        formatter = parser._get_formatter()

        # positionals, optionals and user-defined groups
        for action_group in parser._action_groups:
            formatter.start_section(None)
            formatter.add_text(None)
            formatter.add_arguments(action_group._group_actions)
            formatter.end_section()

        # epilog
        formatter.add_text(parser.epilog)

        # determine help from format above
        return '.SH OPTIONS\n' + formatter.format_help()

    def _format_action_invocation(self, action):
        if not action.option_strings:
            metavar, = self._metavar_formatter(action, action.dest)(1)
            return metavar

        else:
            parts = []

            # if the Optional doesn't take a value, format is:
            #    -s, --long
            if action.nargs == 0:
                parts.extend([self._bold(action_str) for action_str in action.option_strings])

            # if the Optional takes a value, format is:
            #    -s ARGS, --long ARGS
            else:
                default = self._underline(action.dest.upper())
                args_string = self._format_args(action, default)
                for option_string in action.option_strings:
                    parts.append('%s %s' % (self._bold(option_string), args_string))

            return ', '.join(parts)


class ManPageCreator(object):
    """
    This class takes a little different approach. Instead of relying on
    information from ArgumentParser, it relies on information retrieved
    from distutils.
    This class makes it easy for package maintainer to create man pages in cases,
    that there is no ArgumentParser.
    """
    pass

    def _mk_name(self, distribution):
        """
        """
        return '.SH NAME\n%s \\- %s\n' % (distribution.get_name(),
                                          distribution.get_description())

if AUTO_BUILD:
    build.sub_commands.append(('build_manpage', None))
