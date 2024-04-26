import yaml

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




if __name__ == "__main__":
    main()