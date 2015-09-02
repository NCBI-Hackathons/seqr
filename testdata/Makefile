VERSION=4.10.4
MEM=3G
PORT=8983


install: solr-${VERSION}/bin/solr 

start:  install
	solr-${VERSION}/bin/solr -s ./solr -m ${MEM} -p ${PORT}

stop: 
	solr-${VERSION}/bin/solr stop -p ${PORT}


solr-${VERSION}/bin/solr:
	wget http://www.trieuvan.com/apache/lucene/solr/${VERSION}/solr-${VERSION}.tgz  -O- | tar xzf - 

clean:
	solr-${VERSION}/bin/solr stop -p ${PORT}
	rm -r solr-${VERSION}
