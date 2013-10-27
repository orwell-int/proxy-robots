package MessageComponent;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class BluetoothHandler {
        
        private static final BluetoothHandler m_instance = new BluetoothHandler();
        
        protected BTConnection m_btc = null;
    protected DataInputStream m_dis = null;
    protected DataOutputStream m_dos = null;
    
        private BluetoothHandler()
        {
                LCD.drawString(" Waiting for PC ", 4, 10, true);
                
                //Blocking call waiting for PC to connect.
                m_btc = Bluetooth.waitForConnection();
                
                LCD.clearDisplay();
                m_dos = m_btc.openDataOutputStream();
                m_dis = m_btc.openDataInputStream();            
        }
        
        public static BluetoothHandler getInstance()
        {
                return m_instance;
        }
        
        public DataInputStream getInputStream()
        {
                return m_dis;
        }
        
        public DataOutputStream getOutputStream()
        {
                return m_dos;
        }
               
}
