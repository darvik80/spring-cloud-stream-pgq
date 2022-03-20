# Spring Clound Stream Binder for PGQ

### pgq
* [pgq](https://github.com/pgq/pgq)
#### install 
```sh
sudo apt install postgresql-${version}-pgq3 pgqd
```
#### registry
```postgresql
create extension if not exists pgq;
```
#### usage example
```postgresql
--- create queue
select * from pgq.create_queue('event_bus');

--- register consumer
select * from pgq.register_consumer('event_bus', 'cid-smart-home-event_bus');

--- publish message
select * from pgq.insert_event('event_bus', 'dws-report', '{"status": "online"}');

--- consume message {
--- get batch id: example 1
select * from pgq.next_batch('event_bus', 'cid-smart-home-event_bus');

-- get batch events by id
select * from pgq.get_batch_events(1);

-- mark batch as processed
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
```postgresql
create extension if not exists pgq_coop;
```
#### usage example
TBD