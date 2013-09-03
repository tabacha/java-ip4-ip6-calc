#!/bin/sh
javac -cp java-ipv6-0.14.jar:commons-net-3.3.jar IPV6C.java
if [ "$?" == "0" ]; then 
    java -cp java-ipv6-0.14.jar:commons-net-3.3.jar:. IPV6C
fi