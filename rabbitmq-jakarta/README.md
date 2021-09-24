# Build
mvn clean package && docker build -t cz.tc.learn.rabbitmq/rabbitmq-jakarta .

# RUN

docker rm -f rabbitmq-jakarta || true && docker run -d -p 8080:8080 -p 4848:4848 --name rabbitmq-jakarta cz.tc.learn.rabbitmq/rabbitmq-jakarta 