# RabbitMQ labs
Hrátky s králíkem - implementaci queue řešení s použitím AMPQ protocolem. Příklady předpokládají, že rabbitmq server běží na pozadí - pro jednoduchost používám docker image, kterou spouštím:

```
# for RabbitMQ 3.9, the latest series
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.9-management
```

## Zdroje
1. [Downloading and Installing RabbitMQ](https://www.rabbitmq.com/download.html)
2. [Rabbitmq Getting Started](https://www.rabbitmq.com/getstarted.html)
3. [Rabbitmq Tutorials](https://github.com/rabbitmq/rabbitmq-tutorials)
