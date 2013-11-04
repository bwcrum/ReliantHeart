package com.numerex.tc65i.utilities.at;

import com.siemens.icm.io.ATCommand;
import com.siemens.icm.io.ATCommandListener;

public class ATCommands {
	private static ATCommands instance = null;
	private ATCommand  atCommandInterface = null;
	private ATCommandListener atCommandListener = null;

	private static final class ATCommandListenerImpl implements ATCommandListener {
		public void ATEvent(String arg0) {}
		public void CONNChanged(boolean arg0) {}
		public void DCDChanged(boolean arg0) {}
		public void DSRChanged(boolean arg0) {}
		public void RINGChanged(boolean arg0) {}
    }
   
	public static ATCommands getInstance() {
		if (instance == null)
			instance = new ATCommands();
		return instance;
	}
	
	private ATCommands() {
	   try {
	       atCommandListener = new ATCommandListenerImpl();
	       atCommandInterface = new ATCommand(false);
	       atCommandInterface.addListener(atCommandListener);
	   } catch (Exception e) {
		   System.out.println(e);
		   e.printStackTrace();
	   }
   }

   public synchronized String sendRecv(String send) {
	   String recv = null;
	   String testdata = null;
	   try {
			   recv = atCommandInterface.send(send + "\r");
		   recv = recv.trim();
	   } catch (Exception e) {
		   System.out.println(e);
	   } finally {
	   }
	   return recv;
   }
}