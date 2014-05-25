proxy-robots
============

Handles the communication between the server and the (real) robots.

Running it
----------

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
