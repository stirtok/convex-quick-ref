#!/bin/zsh

curl -X 'POST' \
  'http://localhost:8080/api/v1/query' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/cvx' \
  -d '@scratch/query.cvx' \
  -o scratch/doc-tree.json