test-db:
	cd neo4j-community-2.2.2; bin/neo4j console

setup: neo4j
	cp neo4j-community-2.2.2/conf/neo4j-testserver.properties neo4j-community-2.2.2/conf/neo4j-server.properties

neo4j: neo4j.tar.gz
	tar zxvf neo4j.tar.gz

neo4j.tar.gz:
	wget http://neo4j.com/artifact.php?name=neo4j-community-2.2.2-unix.tar.gz -O neo4j.tar.gz
