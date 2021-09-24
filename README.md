# rabbitmq.labs
Hrátky s králíkem. Projekty předpokládají většinou, že rabbitmq běží na pozadí, viz. "Downloading a Installing RabbitMQ". 
Pro jednoduchost používám docker image, kterou spouštím:

```
# for RabbitMQ 3.9, the latest series
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.9-management
```

## Zdroje
1. [Downloading and Installing RabbitMQ](https://www.rabbitmq.com/download.html)
