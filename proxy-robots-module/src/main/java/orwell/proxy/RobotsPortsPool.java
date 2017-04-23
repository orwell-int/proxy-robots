package orwell.proxy;

import java.util.ArrayDeque;

public class RobotsPortsPool {
    ArrayDeque<Integer> availablePorts = new ArrayDeque<>();

    public RobotsPortsPool(int beginPort, int portsCount) {
        assert (portsCount > 0);
        assert (beginPort > 0 && beginPort < 65536);

        for (int portNumber = beginPort; portNumber < beginPort + portsCount; portNumber++) {
            availablePorts.add(portNumber);
        }
    }

    public int getAvailablePort() {
        return availablePorts.pop();
    }
}