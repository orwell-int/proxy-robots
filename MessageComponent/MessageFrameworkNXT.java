package MessageComponent;

import java.io.IOException;
import java.util.ArrayList;

import lejos.nxt.LCD;

public class MessageFrameworkNXT {
        
        private static MessageFrameworkNXT m_instance = new MessageFrameworkNXT();
        
        protected BluetoothHandler m_bth;
        protected boolean m_keepRunning = true;
        protected ArrayList<MessageListenerInterface> m_messageListeners;
        protected Object m_RXguard;
        protected Object m_TXguard;
        
        
        private Reader m_reader;
        private ArrayList<Byte> m_receivedBytes;
        
        private MessageFrameworkNXT()
        {
                m_bth = BluetoothHandler.getInstance();
                m_receivedBytes = new ArrayList<Byte>();
                m_messageListeners = new ArrayList<MessageListenerInterface>();
                m_RXguard = new Object();
                m_TXguard = new Object();
                
                m_reader = new Reader(); 
        m_reader.setDaemon(true);
        }
        
        public static MessageFrameworkNXT getInstance() {
                return m_instance;
        }
        
        public void SendMessage(LIMessage msg)
        {
                synchronized (m_TXguard) {
                        try {
                                m_bth.getOutputStream().write(msg.getEncodedMsg());
                                m_bth.getOutputStream().flush();
                        
                        } catch (IOException e) {
                                LCD.drawString(e.getMessage(), 0, 0);
                        }
                }
        }
        
        public void StartListen()
        {
                m_keepRunning = true;
                m_reader.start(); //Start to listen for incoming messages
        }
        
        private class Reader extends Thread
    {
        public void run()
        {
            while (m_keepRunning)
            {
                try {
                    
                        int input = (byte)m_bth.getInputStream().read();
                        if(input != -1)
                        {
                                m_receivedBytes.add((byte)input);
                                
                                //Check if this is a packet frame end <ETX>
                        if(input == (byte)3) //ETX
                                RecievedNewPacket();
                        }
                        else
                        {
                                //No data. Sleep to save energy.
                                try {
                                                        sleep(10);
                                                } catch (InterruptedException e) {}
                        }
                }
                catch (IOException e) {
                        System.err.println(e.getMessage());
                }
            }        
        }
    }
            
    private void RecievedNewPacket()
    {
        byte[] msgBytes = new byte[m_receivedBytes.size()];
        for(int i=0; i<m_receivedBytes.size(); i++)
        {
                msgBytes[i] = m_receivedBytes.get(i);
        }

        m_receivedBytes.clear();
        
        //only create and transmit the message if it is valid
        if(isPacketValid(msgBytes))
        {
                synchronized (m_RXguard) {
                        LIMessage msg = LIMessage.setEncodedMsg(msgBytes);
                                for(int j=0; j<m_messageListeners.size(); j++)
                                {
                                m_messageListeners.get(j).recievedNewMessage(msg);
                                }
                }
        }
        else
        {
                LCD.drawString("Corrupt package", 0, 0);
        }
    }
            
    private boolean isPacketValid(byte[] packet)
    {
        try
        {
                if(packet[0] != (byte)2) //Does it contain a start frame byte?  
                        return false;
                else if(packet[2] != (byte)':') //Does it contain the command payload seperator?
                        return false;
                else if(packet[packet.length-1] != (byte)3) //Does it contain an end frame byte?
                        return false;
                else
                        return true;
        } catch (Exception e) {
                LCD.drawString("Corrupt package", 0, 0);
                return false;
        }
    }
    
    public void addMessageListener(MessageListenerInterface msgListener)
        {
                m_messageListeners.add(msgListener);
        }
        
        public void StopListen()
        {
                m_keepRunning = true;
        }
}
