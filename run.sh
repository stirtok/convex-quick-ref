#!/bin/zsh

clj -X stirtok.convex-quick-ref/build-query-cvx

curl -X 'POST' \
  'http://localhost:8080/api/v1/query' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/cvx' \
  -d '@scratch/query.cvx' \
  -o scratch/doc-tree.json

clj -X stirtok.convex-quick-ref/build-adoc

cp resources/docinfo.html scratch/

asciidoctor -a allow-uri-read -b html5 scratch/index.adoc -o docs/index.html