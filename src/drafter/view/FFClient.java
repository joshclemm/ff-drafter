/**
 *
 */
package drafter.view;

import java.io.IOException;

import javax.swing.SwingUtilities;

/**
 * @author clemmj
 *
 */
public class FFClient {

	public static void main(String[] args) {

		if(args.length > 0) {
			System.out.println("Usage: ");
			System.out.println("  java view.FFClient <options>");
			System.out.println("\nOptions: ");
			System.out.println("  -DserverHost=<hostname>\t\tConnect to server with specified host.");
			System.out.println("  -DserverPort=<integer>\t\t\tConnect to server with specified port.");
			System.out.println("  -DmodelUpdateProtocol=<protocol>");
			System.out.println("  \tUpdate model with specified protocol.");
			System.out.println("  \t*Note* Coordinate with Server sending model. If server sends single updates,");
			System.out.println("  \tthis property is ignored. Also fullModel options become hiddenOnly if");
			System.out.println("  \tsending protocol is hidden only");
			System.out.println("     Update protocols:");
			System.out.println("        fullModelHiddenOnly - Receive full model, only update hidden column (default)");
			System.out.println("        fullModel - Receive full model, update entire model");
			System.out.println("        hiddenOnly - Receive and update only hidden column");

			System.exit(0);
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new FFDisplayFrame("FFClient2",true);
				} catch (IOException e) {
					System.err.println("Could not connect to Server");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
