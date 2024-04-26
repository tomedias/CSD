#!/bin/sh
for i in 1 2 3 4
do
openssl genrsa -out "server$i.key" 2048
openssl req -new -key "server$i.key" -out "server$i.csr"
openssl x509 -req -days 365 -in "server$i.csr" -signkey "server$i.key" -out "server$i.crt"
keytool -import -alias "server$i" -file "server$i.crt" -keystore truststore -storepass changeit
done