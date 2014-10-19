package orwell.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orwell.common.MessageListenerInterface;
import orwell.common.UnitMessage;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;

public class MessageFramework {
	
	final static Logger logback = LoggerFactory.getLogger(MessageFramework.class); 
	
	// private static MessageFramework m_instance = new MessageFramework();
	private Object m_RXguard;
	private Object m_TXguard;

	protected ArrayList<MessageListenerInterface> m_messageListeners;
	protected NXTConnector m_connecter;
	protected InputStream m_is;
	protected OutputStream m_os;
	protected boolean m_connected;

	private Reader m_reader;
	private ArrayList<Byte> m_receivedBytes;

	public MessageFramework() {

		m_messageListeners = new ArrayList<MessageListenerInterface>();
		m_receivedBytes = new ArrayList<Byte>();
		m_RXguard = new Object();
		m_TXguard = new Object();

		m_connecter = new NXTConnector();
		m_connected = false;

		m_reader = new Reader();
		m_reader.setDaemon(true);
	}

	// public static synchronized MessageFramework getInstance() {
	// return m_instance;
	// }

	public boolean ConnectToNXT(NXTInfo info) {
		if (!m_connecter.connectTo(info.name, info.deviceAddress,
				NXTCommFactory.BLUETOOTH)) {
			return false;
		}
		m_is = m_connecter.getInputStream();
		m_os = m_connecter.getOutputStream();

		// We could implement a handshake to verify the connection before we
		// approve the connection.
		m_connected = true;

		try {
			m_reader.start(); // Start to listen for incomming messages
		} catch (IllegalThreadStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (m_is == null || m_os == null)
			return false;

		return m_connected;
	}

	public void SendMessage(UnitMessage msg) {
		try // handshake
		{
			byte[] outgoing = msg.getEncodedMsg();

			m_os.write(outgoing);
			m_os.flush();

		} catch (IOException e) {
			e.printStackTrace();
			logback.error("SendMessage failed: " + e.getMessage());
			// m_connected = false;
		}
	}

	public void addMessageListener(MessageListenerInterface msgListener) {
		m_messageListeners.add(msgListener);
	}

	private class Reader extends Thread {
		@Override
		public void run() {
			// while (true)
			// {
			while (m_connected) {
				try {

					byte input;
					// TODO: We should address the blocking issue.
					while ((input = (byte) m_is.read()) >= 0) {
						// Adding package to received array
						m_receivedBytes.add(input);

						// Check if this is a packet frame end <ETX>
						if (input == (byte) 3) // ETX
							RecievedNewPacket();
					}
					close();

				} catch (IOException e) {
					e.printStackTrace();
					logback.error("Error while reading in MessageFramework: "
									+ e.getMessage());
					close();
				}
			}
			Thread.yield();
			// }
		}
	}

	private void RecievedNewPacket() {
		byte[] msgBytes = new byte[m_receivedBytes.size()];
		for (int i = 0; i < m_receivedBytes.size(); i++) {
			msgBytes[i] = m_receivedBytes.get(i);
		}

		m_receivedBytes.clear();

		// only create and transmit the message if it is valid
		if (isPacketValid(msgBytes)) {
			synchronized (m_RXguard) {
				UnitMessage msg = UnitMessage.setEncodedMsg(msgBytes);
				for (int j = 0; j < m_messageListeners.size(); j++) {
					m_messageListeners.get(j).recievedNewMessage(msg);
				}
			}
		} else {
			logback.debug("Invalid Packet:" + new String(msgBytes));
		}
	}

	private boolean isPacketValid(byte[] packet) {
		try {
			if (packet[0] != (byte) 2) // Does it contain a start frame byte?
				return false;
			else if (packet[2] != (byte) ':') // Does it contain the command
												// payload seperator?
				return false;
			else if (packet[packet.length - 1] != (byte) 3) // Does it contain
															// an end frame
															// byte?
				return false;
			else
				return true;
		} catch (Exception e) {
			logback.error("Received corrupt package: "
					+ getBytesString(packet));
			return false;
		}
	}

	private String getBytesString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(String.valueOf(bytes[i] + ", "));
		}

		return sb.toString();
	}

	public void close() {

		try {
			m_connected = false;
			m_is.close();
			m_os.close();

			if (m_connecter != null)
				m_connecter.close();
		} catch (IOException e) {
			e.printStackTrace();
			logback.error("Error while closing MessageFramework: "
					+ e.getMessage());
		}
	}
}
