package drafter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

import drafter.view.FFDisplayFrame;

public class FFDrafter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length > 0) {
			System.out.println("Usage: ");
			System.out.println("  java view.FF <options>");
			System.out.println("\nOptions: ");
			System.out.println("  -Dstandalone=<true/false>\t\tRun in standalone mode (no server).");
			System.out.println("  -DserverPort=<integer>\t\tStart server with specified port.");
			System.out.println("  -DmodelSendProtocol=<protocol>\tSend model updates with specified protocol.");
			System.out.println("     Update protocols:");
			System.out.println("        singleUpdate - Send single cell updates (default)");
			System.out.println("        fullModel - Send entire table model");
			System.out.println("        hiddenOnly - Send hidden column data only");
			System.out.println("  -DextraSource=<source>\tAdd view details from extra source.");

			System.exit(0);
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					System.setProperty("standalone", "true");
					JFrame.setDefaultLookAndFeelDecorated(true);
					UIManager.setLookAndFeel(new NimbusLookAndFeel());
					new FFDisplayFrame("FF Drafter");
				} catch (Exception e) {
					System.err.println("Could not start Server");
					e.printStackTrace();
				}
			}
		});
	}

}
