[![Build Status](https://travis-ci.org/orwell-int/proxy-robots.svg?branch=master)](https://travis-ci.org/orwell-int/proxy-robots) [![Stories in Ready](https://badge.waffle.io/orwell-int/proxy-robots.png?label=ready&title=Ready)](https://waffle.io/orwell-int/proxy-robots) [![Coverage Status](https://coveralls.io/repos/orwell-int/proxy-robots/badge.svg?branch=master)](https://coveralls.io/r/orwell-int/proxy-robots?branch=master)
proxy-robots
============

Handles the communication between the server and the (real) robots.

local setup for coveralls
-------------------------
Run with maven
--------------
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

Run maven (validate, build, test)
```
maven clean install
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

Running it with ant (soon to be deprecated)
-------------------------------------------

Show the existing targets:
```
ant -p
```

Build, upload and run the program on the robot:
```
ant uploadandrun_robots
```

Build the proxy:
```
ant compile_proxy
```

Run the proxy:
```
ant run_proxy 
```

Run the junit test:
```
ant junit-proxy -v
```

Build a test report
```
ant junitreport 
```
