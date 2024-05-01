# Project Assignment 1 | Francisco Vasco 61028 & Tomé Dias 60719 | Dependable Distributed Systems 2023-24

## Compilation Script

To compile all the code and put the servers/replicas up and running on Docker, use the following
command.
This script will compile the java code, rebuild the images and launch the containers through Docker Compose.

```
sh run.sh
```

## Whats implemented

- The system is composed of 4 rest servers that act as proxies for the bftsmart replica system.
- Every one of these systems keeps an updated copy of the ledger.
- Writing operations are ordered and are writen into the ledger.
- Reading operations are not ordered and only return the ledger.
- The client generates an Operation ID and sends his request to the proxy of his choice.
- In the answer he gets he can see the Operation ID, the value of return and the hash of the ledge containing his answer
  (when the operation is a writing operation) or the Operation ID of the previous request, the value of return and the hash
of the ledge containing his answer (when the operation is a reading operation).
- To confirm that his operation was correctly made the client is able to send a request to another proxy (or more if he wants to)
in order to confirm that the operationID he got and the hash of the ledger are matching.


## Vulnerabilities to fix
- The admin operation is still not secure, we didn't have time to implement the security measures, but it should be just 
similar as the client transfer, where the admin has to sign with his private key in order to show he is really the admin.
- An insider may hack one of the rest servers and make requests using "fake accounts" from him to him, in order to make
the ledge believe he has more money than him. In order to prevent this we could implement a policy where two or more servers
have to agree on the operation before it is written in the ledger.
- There is still some exception catching that could be done in order to keep the system from crashing.


## How to run the client
You can test the system by running the test class and executing the orders there.
