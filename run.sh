mvn initialize \
   -Dfile="bin\BFT-SMaRt.jar"\
   -DgroupId=bftsmart \
   -DartifactId=bftsmart \
   -Dversion=1.2 \
   -Dpackaging=jar \
   -DgeneratePom=true
python3 ./generate_docker.py
mvn clean install
docker build -t myapp .
docker-compose down
docker-compose up