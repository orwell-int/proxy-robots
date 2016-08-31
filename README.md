[![Build Status](https://travis-ci.org/orwell-int/proxy-robots.svg?branch=master)](https://travis-ci.org/orwell-int/proxy-robots) [![Stories in Ready](https://badge.waffle.io/orwell-int/proxy-robots.png?label=ready&title=Ready)](https://waffle.io/orwell-int/proxy-robots) [![Coverage Status](https://coveralls.io/repos/orwell-int/proxy-robots/badge.svg?branch=master)](https://coveralls.io/r/orwell-int/proxy-robots?branch=master)
proxy-robots
============

Handles the communication between the server and the (real) robots.

Checkout the code
-----------------
Get the sources
```
git clone git@github.com:orwell-int/proxy-robots.git
```

Get the submodules
```
git submodule update --init --recursive
```

local setup for coveralls
-------------------------
Run with maven
--------------
Prerequiste: have jdk-7+ installed on your machine
```
javac -version
>javac 1.7.xxx

java -version                                                         
>java version "1.7.xxx"
```

Install maven:
```
sudo apt-get install maven
```

Download leJOS tar.gz
```
wget -nc --no-check-certificate http://sourceforge.net/projects/lejos/files/lejos-NXJ/0.9.1beta/leJOS_NXJ_0.9.1beta-3.tar.gz/download -O ./leJOS_NXJ_0.9.1beta-3.tar.gz
tar -xvf leJOS_NXJ_0.9.1beta-3.tar.gz
export NXJ_HOME=leJOS_NXJ_0.9.1beta-3
```

Run maven
```
mvn validate
mvn clean install
```

To update the coveralls status, export your repo token in the following environment variable:
(You will find it on https://coveralls.io/r/orwell-int/proxy-robots)
```
export COVERALLS_REPO_TOKEN=yourToken
```

To update the coveralls status, export your repo token in the following environment variable:
(You will find it on https://coveralls.io/r/orwell-int/proxy-robots)
```
mvn clean cobertura:cobertura coveralls:report
```

Create a config file, using the template in proxy-robots-module/src/main/resources/config.default.xml
```
Rename the config you edited to suit your setup to proxy-robots-module/src/main/resources/config.xml
```

Run the jar created by the install to start the application
```
java -jar proxy-robots-module/target/proxy-robots-module-0.1.0-jar-with-dependencies.jar -f proxy-robots-module/src/main/resources/config.xml
```

