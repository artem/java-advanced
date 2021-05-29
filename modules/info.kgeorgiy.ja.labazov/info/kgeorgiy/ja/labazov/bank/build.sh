#!/bin/bash
javac -cp .. Server.java Client.java BankWebServer.java
#rmic -d $CLASSPATH examples.rmi.RemoteAccount examples.rmi.RemoteBank
