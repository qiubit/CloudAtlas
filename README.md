### Agent

Install and run rabbitmq-server

```sh
$ sudo apt-get install rabbitmq-server
$ sudo service rabbitmq-server start
```

Install and run Redis: https://redis.io/topics/
However, Redis is not needed for compiling and running Interpreter.

To compile Agent, do:
```sh
$ mvn install
```

To run Agent, do:
```sh
$ ./start_agent.sh
```

### Interpreter

After compiling agent (instructions above), execute `./start_interpreter.sh` to  run interpreter.

### WWW client

In order to use WWW client you need to have `yarn` installed. To run WWW service, do:
```sh
$ cd ./www/portal
$ yarn
$ yarn start
```

After doing that, by you should be able to access CloudAtlas Portal by entering `http://localhost:3000` in the browser.
