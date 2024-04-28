import yaml
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.backends import default_backend
import os
class MyDumper(yaml.Dumper):

    def increase_indent(self, flow=False, indentless=False):
        return super(MyDumper, self).increase_indent(flow, False)

def read_yaml(file_path):
    with open(file_path, 'r') as file:
        data = yaml.load(file, Loader=yaml.FullLoader)
    return data

def write_yaml(data, file_path):
    with open(file_path, 'w', newline="\n") as file:
        yaml.dump(data, file, Dumper=MyDumper,default_flow_style=False, width=10000,sort_keys=False)



def generate_key_pair():
    try:
        # Generate an ECDSA key pair for the curve 'prime256v1' (NIST P-256)
        private_key = ec.generate_private_key(ec.SECP256R1(), default_backend())

        # Optional: Serialize private and public key
        private_key_pem = private_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.NoEncryption()
        ).decode('utf-8')
        
        public_key = private_key.public_key()
        public_key_pem = public_key.public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        ).decode('utf-8')
        return private_key_pem, public_key_pem
        
    except Exception as e:
        print(f"An error occurred: {e}")
        return None

    


def main():
    config = read_yaml("./config.yaml")
    docker_compose = {
        'services': {

        },
        'networks': {
            'app-network': {
                'driver': 'bridge'
            },
        },
    }

    replicas = config['replicas']
    proxys = config['proxys']
    for i in range(replicas):
        port = 11000 + i*10
        docker_compose['services'][f'replica{i}'] = {
            'image': 'myapp:latest',
            'ports': [f"{port}:{port}"],
            'command': f'java -Dlogback.configurationFile=\"./config/logback.xml\" -jar /app/ReplicaServer-jar-with-dependencies.jar {i}',
            'networks': ['app-network'],
        }
    for i in range(proxys):
        port = 3456 + i
        docker_compose['services'][f'restServer_{i+1}'] = {
            'image': 'myapp:latest',
            'ports': [f"{port}:{port}"],
            'command': f'java -Dlogback.configurationFile=\"./config/logback.xml\" -jar /app/RestWalletServer-jar-with-dependencies.jar {i+1} {port}',
            'networks': ['app-network'],
        }

    write_yaml(docker_compose, './docker-compose.yml')  
    for x in range(1,proxys+1):
        keys = generate_key_pair()
        if not os.path.isdir(f"./tls/rest{x}"):
            os.makedirs(f"./tls/rest{x}")
                     
        if keys:
            private_key, public_key = keys
            key_body = "".join(private_key.splitlines()[1:-1])
            with open(f'./tls/rest{x}/privatekey', "w") as f:
                f.write(key_body)
            key_body = "".join(public_key.splitlines()[1:-1])
            with open(f'./tls/rest{x}/publickey', "w") as f:
                f.write(key_body)

if __name__ == "__main__":
    main()
            
            