package edu.rit.csci759.jsonrpc.server;

import java.io.*;
import java.net.*;

import edu.rit.csci759.fuzzylogic.MyTipperClass;

public class ExecutionThread extends Thread {


	private Socket socketTCP;
	private  ObjectInputStream ois;
	private ObjectOutputStream oos;
	private CallBack callback;

	public ExecutionThread(Socket socket) throws IOException {
		this.socketTCP = socket;
		ois = new ObjectInputStream(this.socketTCP.getInputStream());
		oos = new ObjectOutputStream(this.socketTCP.getOutputStream());
	}


	public void run() {

		try {

			// Independent thread sending the timestamp, light Intensity 
			// and temperature to the android client
			MyTipperClass.update_blind();
			new SendDataUpdate(socketTCP).start();
			System.out.println("send data to android");

			// creating a callback
			callback = new CallBack(socketTCP);

			Object obj = null;
			while(true) {
				obj = ois.readObject();
			// Update the current status of the blind	
				MyTipperClass.update_blind();		
				while(obj != null) {
				// Checking for all class object, statement with if will be executed when fuzzy rules are added or updated.	
					if (obj.getClass().equals(AllRules.class)) {
						AllRules changeFuzzy = (AllRules) obj;
						MyTipperClass.addFuzzyRule(changeFuzzy.allRules);
						MyTipperClass.update_blind();

					} else if (obj.getClass().equals(String.class)) {
						if (obj.equals("view")) {
							oos.writeObject(new AllRules(MyTipperClass.getRules()));
						} else if (obj.equals("subscribe")) {
							callback.setNotify(true);
						} else if (obj.equals("unsubscribe")) {
							callback.setNotify(false);
						}
					}
				}
			}

		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
