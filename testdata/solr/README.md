# README #

```
#!shell
export SOLR_BIN=//hackathon/solr-4.10.4/bin
export SOLR_HOME=//hackathon/SOLR_HOME
```


```
#!shell
#start
$SOLR_BIN/solr start -s $SOLR_HOME
$SOLR_BIN/solr start -m 5G -s $SOLR_HOME
#stop
$SOLR_BIN/solr stop -p 8983
#cleanup
$SOLR_HOME/bin/delete.sh
#update
$SOLR_HOME/bin/post.sh *.json

```