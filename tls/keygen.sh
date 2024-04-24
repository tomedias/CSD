#!/bin/sh
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
keytool -import -alias your-alias -file server.crt -keystore truststore -storepass changeit


