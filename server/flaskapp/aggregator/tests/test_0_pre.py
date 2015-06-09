# -*- coding: utf-8 -*-


def test_can_connect(graph):
    assert graph.neo4j_version


def test_delete_all(graph):
    graph.delete_all()
    assert graph.order == 0
    assert graph.size == 0
