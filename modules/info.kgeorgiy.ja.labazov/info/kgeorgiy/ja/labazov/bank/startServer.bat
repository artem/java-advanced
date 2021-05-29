@echo off
set classpath=..

start %java_home%\bin\rmiregistry
start %java_home%\bin\java rmi.Server
