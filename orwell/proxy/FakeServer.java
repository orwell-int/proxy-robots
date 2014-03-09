package orwell.proxy;

import org.zeromq.ZMQ;

import orwell.messages.Controller;
import orwell.messages.Controller.Input;

public class FakeServer
{
	public FakeServer()
	{
	}

	static Controller.Input buildTestInput()
	{
		Controller.Input.Builder testInput = Input.newBuilder();
		Controller.Input.Move.Builder testMove = Controller.Input.Move.newBuilder();
		testMove.setLeft(50);
		testMove.setRight(0.234);

		testInput.setMove(testMove.build());

		Controller.Input.Fire.Builder testFire = Controller.Input.Fire.newBuilder();
		testFire.setWeapon1(false);
		testFire.setWeapon2(true);
		testInput.setFire(testFire);
		
		return testInput.build();
	}
	
	static byte [] getZMQmessage(String dest, Controller.Input message)
	{
		String zMQmessageHeader = dest + " " + "Input" + " ";
		return orwell.proxy.Utils.Concatenate(zMQmessageHeader.getBytes(), message.toByteArray());
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception
	{
          ZMQ.Context context = ZMQ.context(1);

        //  Socket to receive messages on
        ZMQ.Socket receiver = context.socket(ZMQ.PULL);
        ZMQ.Socket sender = context.socket(ZMQ.PUB);
        receiver.setLinger(1000);
        sender.setLinger(1000);
        receiver.bind("tcp://localhost:9000");
        System.out.println("Server Receiver created");
        sender.bind("tcp://*:9001");  
        System.out.println("Server Sender created");


        //  Process tasks forever
        while (!Thread.currentThread().isInterrupted())
        {
            String zmq_message = receiver.recvStr();
            System.out.flush();
            System.out.println("Message received: '" + zmq_message + "'");
            
            if (0 == zmq_message.compareTo("Banana"))
            {
                Controller.Input input = buildTestInput();
                System.out.println("Building input for test");

                byte [] zmq_input = getZMQmessage("BananaOne", input);
                String str = new String(zmq_input, 0, zmq_input.length);
                System.out.println("Message is:" + str);
//                System.out.println("raw length: " + str.getBytes().length);
                sender.send(zmq_input);
                System.out.println(Controller.Input.parseFrom(input.toByteArray()).toString());
                System.out.println("Message sent");
            }
        }
        
        
        sender.close();
        receiver.close();
        context.term();
	}

}
