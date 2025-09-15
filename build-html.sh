#!/bin/zsh

cp resources/docinfo.html \
    scratch/
    
asciidoctor -a allow-uri-read -b html5 \
		scratch/index.adoc \
		-o scratch/index.html