/**
 *
 */
package drafter.comms;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.table.DefaultTableModel;

import drafter.view.RankingsTable;


/**
 * @author Josh Clemm
 *
 */
public class Client {

	private enum ModelUpdateProtocol { FullModelHiddenOnly, FullModel, HiddenOnly};

	private ArrayList<RankingsTable> tableList;
	protected AtomicBoolean keepRunning;
	private ObjectInputStream ois;
	private ModelUpdateProtocol modelUpdateProtocol;

	private static final String defaultServerPort = "2222";
	private static final String defaultServerHost = "localhost";

	public Client(ArrayList<RankingsTable> tableList) throws UnknownHostException, IOException, ClassNotFoundException {

		modelUpdateProtocol = getModelUpdateProtocol();

		keepRunning = new AtomicBoolean(true);
		Socket socket = new Socket(getServerHost(),getServerPort());

		System.out.println("Connected to server ("+getServerHost()+":"+getServerPort()+").");
		ois = new ObjectInputStream(socket.getInputStream());

		for (RankingsTable rankingsTable : tableList) {
			DefaultTableModel initialModel = getInitialModel();
			rankingsTable.setAutoCreateColumnsFromModel(false);
			if(rankingsTable != null) {
				initialModel.setColumnIdentifiers(rankingsTable.getColumnIdentifiers());
				rankingsTable.setModel(initialModel);
			}
		}

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new TableModelUpdater());

		this.tableList = tableList;
	}

	private void updateModel(CellUpdate update) {

		RankingsTable rankingsTable = tableList.get(update.getModelIndex());
		rankingsTable.getModel().setValueAt(update.getValue(),
				update.getRow(),
				update.getCol());

	}

	private void updateModel(DefaultTableModel model) {
		if(modelUpdateProtocol.equals(ModelUpdateProtocol.FullModelHiddenOnly)) {
			if(model.getColumnCount() == 1) {
				//incorrectly set protocol, we are only getting a model with 1 col
				// so let's use hidden only protocol instead.
				//				System.out.println("using hidden only instead");
				modelUpdateProtocol = ModelUpdateProtocol.HiddenOnly;

				updateModel(model);
				return;
			}
			else {
				int hiddenColumn = tableList.get(0).getColumnExt("Hidden").getModelIndex();
				for (int i = 0; i < model.getRowCount(); i++) {

					Object valueAt = model.getValueAt(i, model.getColumnCount()-1);
					tableList.get(0).getModel().setValueAt(valueAt,
							tableList.get(0).convertRowIndexToModel(i),
							hiddenColumn);
				}
			}
		}
		else if(modelUpdateProtocol.equals(ModelUpdateProtocol.FullModel)) {
			if(model.getColumnCount() == 1) {
				//incorrectly set protocol, we are only getting a model with 1 col
				// so let's use hidden only protocol instead.
				//				System.out.println("using hidden only instead");
				modelUpdateProtocol = ModelUpdateProtocol.HiddenOnly;

				updateModel(model);
				return;
			}
			else {
				tableList.get(0).setModel(model);
			}
		}
		else {
			int hiddenColumn = tableList.get(0).getColumnModel().getColumnIndex("Hidden");
			System.out.println(hiddenColumn);
			//			for (int c = 0; c < table.getColumnModel().getColumnCount(); c++) {
			//				System.out.println(c+": "+table.getColumnModel().getColumn(c).getHeaderValue());
			//			}
			for (int i = 0; i < model.getRowCount(); i++) {
				//				System.out.println("setValueAt("+model.getValueAt(i, 0)+","+i+","+hiddenColumn);
				tableList.get(0).getModel().setValueAt(model.getValueAt(i, 0),i,hiddenColumn);
			}
		}
	}

	private ModelUpdateProtocol getModelUpdateProtocol() {
		ModelUpdateProtocol modelUpdateProtocol;
		String property = System.getProperty("modelUpdateProtocol", "fullModelHiddenOnly");

		if(property.equalsIgnoreCase("hiddenOnly")) {
			modelUpdateProtocol = ModelUpdateProtocol.HiddenOnly;
		}
		else if(property.equalsIgnoreCase("fullModel")) {
			modelUpdateProtocol = ModelUpdateProtocol.FullModel;
		}
		else {
			modelUpdateProtocol = ModelUpdateProtocol.FullModelHiddenOnly;
		}
		return modelUpdateProtocol;
	}

	private int getServerPort() {
		String serverPort = System.getProperty("serverPort", defaultServerPort);
		int port;
		try {
			port = Integer.parseInt(serverPort);
		}
		catch (NumberFormatException e) {
			port = Integer.parseInt(defaultServerPort);
		}
		return port;
	}

	private String getServerHost() {
		return System.getProperty("serverHost", defaultServerHost);
	}

	private DefaultTableModel getInitialModel() {
		Object readObject;
		try {
			readObject = ois.readObject();
			if(readObject instanceof DefaultTableModel) {
				return (DefaultTableModel) readObject;
			}
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}

		return null;
	}

	private class TableModelUpdater implements Runnable {

		@Override
		public void run() {
			while(keepRunning.get()) {
				try {
					Object readObject = ois.readObject();
					if(readObject instanceof DefaultTableModel) {
						DefaultTableModel model = (DefaultTableModel) readObject;
						updateModel(model);
					}
					else if(readObject instanceof CellUpdate) {
						CellUpdate update = (CellUpdate) readObject;
						updateModel(update);
					}
				} catch (EOFException e) {
					// server connection closed
					System.out.println("Disconnected.");
					keepRunning.set(false);
				} catch (IOException e) {
					e.printStackTrace();
					keepRunning.set(false);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					keepRunning.set(false);
				}
			}
		}
	}
}
