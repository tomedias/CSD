#!/bin/sh
genSelfSignedKeysAndCerts(){
    # 1-alias, 2-keystore, 3-cert, 4-storepass, 5-folder path

    # Start
    cd $5

    # Generate a self-signed keystore
    printf "$owner_name\n$owner_org_unit\n$owner_org\n$owner_city\n$owner_district\n$owner_country_code\nyes\n" |\
    keytool -genkeypair -noprompt -keyalg RSA -alias $1 -keystore "selfsigned" -storepass $4 -validity 360 -keysize 2048

    # Convert keystore from PKCS12
    printf "${4}\n${4}\n${4}\n" |\
    keytool -importkeystore -srckeystore selfsigned -srcstoretype PKCS12 -destkeystore "keystore" -deststoretype JKS
    rm selfsigned

    # Generate a certificate from the self-signed keystore
    printf "${4}\n" |\
    keytool -export -alias $1 -keystore "keystore" -file $3

    cd ..
}
resetFolder(){
    rm -r $1
    mkdir $1
}
pass=changeit


rm truststore
resetFolder "rest1"
genSelfSignedKeysAndCerts "rest1" "rest1.jks" "rest1.cer" "changeit" "rest1"

printf "${pass}\n${pass}\nyes\n" |\
keytool -import -alias "rest1" -file "./rest1/rest1.cer" -keystore truststore
for i in 2 3 4
do
  resetFolder "rest$i"
  genSelfSignedKeysAndCerts "rest$i" "rest$i.jks" "rest$i.cer" "changeit" "rest$i"
  printf "${pass}\nyes\n" |\
  keytool -import -alias "rest$i" -file "./rest$i/rest$i.cer" -keystore truststore
done