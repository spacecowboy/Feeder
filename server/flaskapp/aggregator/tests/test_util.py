from ..util import escaped, escapedict


def test_escapedict():
    # One dict as arg
    res = escapedict(dict(a='a', b='b'))
    assert res['a'] == '"a"'
    assert res['b'] == '"b"'

    # One dict as kwargs
    res = escapedict(a='a', b='b')
    assert res['a'] == '"a"'
    assert res['b'] == '"b"'

    # Several dicts
    res = escapedict(dict(a='a', b='b'),
                     dict(c='c', d='d'))
    assert len(res) == 2
    assert res[0]['a'] == '"a"'
    assert res[0]['b'] == '"b"'
    assert res[1]['c'] == '"c"'
    assert res[1]['d'] == '"d"'


def test_escaped():

    @escaped
    def mytest(arg):
        return arg

    assert '"bob"' == mytest('bob')
    assert 'null' == mytest(None)
