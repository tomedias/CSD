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
        yaml.dump(data, file, Dumper=MyDumper,allow_unicode=True, default_flow_style=False, width=10000,sort_keys=False)

def nginx_conf(proxys):
    config_content = """
user nginx;
events {
    worker_connections 1024; # Esperemos que nunca chegue aos 1024 pedidos simultâneos :3
}
http {
    upstream rest_servers {
"""
    for i in range(proxys):
        port = 3456 + i
        if i == 0:
            config_content += f"        server restServer_{i+1}:{port};"
        else:
            config_content += f"\n        server restServer_{i+1}:{port};"


    config_content +="""
    }
    server {
        listen 443 ssl;
        ssl_certificate /etc/nginx/tls/server.crt;
        ssl_certificate_key /etc/nginx/tls/server.key;
        ssl_protocols TLSv1.2 TLSv1.3;
        location / {
            proxy_pass http://rest_servers/rest/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
"""
    return config_content

def main():
    config = read_yaml("./config.yaml")
    docker_compose = {
        'services': {

        },
        'networks': {
            'app-network': {
                'driver': 'bridge'
            },
            'loadbalancer': {
                'driver': 'bridge'
            }
        },
    }
    nginx ={
        'nginx': {
            'image': 'nginx:latest',
            'ports': ["443:443"],
            'volumes': [
                './nginx.conf:/etc/nginx/nginx.conf',
                './tls:/etc/nginx/tls'
            ],
            'depends_on': [
            ],
            'networks': ['app-network', 'loadbalancer'],
        },
    }
    #print(docker_compose)
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
            'networks': ['app-network','loadbalancer'],
        }
        nginx['nginx']['depends_on'].append(f'restServer_{i}')

    docker_compose['services'].update(nginx)
    write_yaml(docker_compose, './docker-compose.yml')
    nginx_config = nginx_conf(proxys)
    with open('./nginx.conf', 'w', newline="\n") as file:
        file.write(nginx_config.strip())



if __name__ == "__main__":
    main()