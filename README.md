### Agent

Install and run rabbitmq-server

```sh
$ sudo apt-get install rabbitmq-server
$ sudo service rabbitmq-server start
```

To compile Agent, do:
```sh
$ mvn install
```

Generate rsa keys for agent and signer
```sh
$ ./gen_keys.sh
```

To run Agent, do:
```sh
$ ./start_agent.sh
```


To run Signer, do:
```sh
$ ./start_signer.sh
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
