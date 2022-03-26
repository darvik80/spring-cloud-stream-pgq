# Spring Clound Stream Binder for PGQ

## binder configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_home
    username: postgres
    password: postgres

  cloud:
    stream:
      pgq:
        default:
          consumer:
            consumer-id: node-1
      bindings:
        eventBusTest-in-0:
          destination: event_bus_coop
          group: cid-event_bus-smart-home_test
        event_bus-in-0:
          destination: event_bus_coop
          group: cid-event_bus-smart-home
        event_bus-out-0:
          destination: event_bus_coop
      pollable-source: event_bus
```

### pgq

* [pgq](https://github.com/pgq/pgq)

#### install

```sh
sudo apt install postgresql-${version}-pgq3 pgqd
```

#### registry

```sql
create extension if not exists pgq;
```

#### usage example

```sql
--- create queue
select * from pgq.create_queue('event_bus');

--- register consumer
select * from pgq.register_consumer('event_bus', 'cid-smart-home-event_bus');

--- publish message
select * from pgq.insert_event('event_bus', 'dws-report', '{"status": "online"}');

--- consume message {
--- get batch id: example 1
select * from pgq.next_batch('event_bus', 'cid-smart-home-event_bus');

--- get batch events by id
select * from pgq.get_batch_events(1);

--- mark batch as processed
select * from pgq.finish_batch(1);
--- } consume message
```

### pgq_coop

* [pgq_coop](https://github.com/pgq/pgq-coop)

#### install

```shell
sudo apt install postgresql-server-dev-${version}
git clone https://github.com/pgq/pgq-coop.git
cd pgq-coop
sudo make install
```

#### registry

```sql
create extension if not exists pgq_coop;
```
#### usage example

```sql
--- create queue
select * from pgq.create_queue('event_bus');

--- register consumer
select * from pgq.register_subconsumer('event_bus', 'cid-smart-home-event_bus', 'node-1');

--- publish message
select * from pgq.insert_event('event_bus', 'dws-report', '{"status": "online"}');

--- consume message {
--- get batch id: example 1
select * from pgq_coop.next_batch('event_bus', 'cid-smart-home-event_bus', 'node-1');

--- get batch id: example 1
select * from pgq_coop.next_batch('event_bus', 'cid-smart-home-event_bus', 'node-1', '1s');

--- get batch events by id
select * from pgq.get_batch_events(1);

--- mark batch as processed
select * from pgq_coop.finish_batch(1);
--- } consume message
```
