/**
 *
 */
package drafter.comms;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import drafter.view.RankingsTable;


/**
 * @author Josh Clemm
 *
 */
public class Server implements Runnable {

	private enum ModelSendProtocol { FullModel, HiddenOnly, SingleUpdate};

	private ServerSocket server;
	private List<MySocketHandler> handlerList;
	private AtomicBoolean keepingRunning;
	private List<DefaultTableModel> initialModelList;

	private static final String defaultServerPort = "2222";
	private ExecutorService executor;
	private ModelSendProtocol modelSendProtocol;

	public Server(ArrayList<RankingsTable> tableList) throws IOException {
		server = new ServerSocket(getServerPort());
		handlerList = new ArrayList<MySocketHandler>();
		initialModelList = new ArrayList<DefaultTableModel>();
		keepingRunning = new AtomicBoolean(true);
		modelSendProtocol = getModelProtocol();

		for (RankingsTable rankingsTable : tableList) {
			initialModelList.add(copyModel(rankingsTable.getModel()));
		}

		executor = Executors.newSingleThreadExecutor();
		executor.execute(this);
		System.out.println("Started server on port " + getServerPort());
	}

	private ModelSendProtocol getModelProtocol() {
		ModelSendProtocol modelSyncProtocol;
		String property = System.getProperty("modelSendProtocol", "singleUpdate");

		if(property.equalsIgnoreCase("hiddenOnly")) {
			modelSyncProtocol = ModelSendProtocol.HiddenOnly;
		}
		else if(property.equalsIgnoreCase("singleUpdate")) {
			modelSyncProtocol = ModelSendProtocol.SingleUpdate;
		}
		else {
			modelSyncProtocol = ModelSendProtocol.FullModel;
		}
		return modelSyncProtocol;
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

	@Override
	public void run() {
		while(keepingRunning.get()) {
			try {
				Socket socket = server.accept();
				synchronized (handlerList) {
					handlerList.add(new MySocketHandler(socket));
				}

			} catch (IOException e) {
				e.printStackTrace();
				keepingRunning.set(false);
			}
		}
	}

	/**
	 * Deep copies the table model because the original model will not serialize
	 * correctly because it is tied to the JTable.  This is not efficient and
	 * a better method should replace it.
	 * @param model
	 * @return
	 */
	private DefaultTableModel copyModel(DefaultTableModel model) {
		DefaultTableModel deepCopy = new DefaultTableModel(model.getRowCount(),model.getColumnCount());
		for (int c = 0; c < model.getColumnCount(); c++) {
			for (int r = 0; r < model.getRowCount(); r++) {
				deepCopy.setValueAt(model.getValueAt(r, c), r, c);
			}
		}
		return deepCopy;
	}

	private DefaultTableModel copyHiddenColumnOnly(DefaultTableModel model) {
		int hiddenColumn = model.findColumn("Hidden");
		DefaultTableModel hiddenCopy = new DefaultTableModel(model.getRowCount(),1);
		for (int r = 0; r < model.getRowCount(); r++) {
			hiddenCopy.setValueAt(model.getValueAt(r, hiddenColumn), r, 0);
		}
		return hiddenCopy;
	}

	public void updateModel(TableModelEvent event) {
		Object source = event.getSource();

		if(source instanceof DefaultTableModel) {
			DefaultTableModel model = (DefaultTableModel) source;

			// Determine which protocol to send
			if(modelSendProtocol.equals(ModelSendProtocol.HiddenOnly)) {
				sendToAllClients(copyHiddenColumnOnly(model));
			}
			else if(modelSendProtocol.equals(ModelSendProtocol.SingleUpdate)) {

				int modelIndex = 0;
				int column = event.getColumn();
				int firstRow = event.getFirstRow();
				Object valueAt = model.getValueAt(firstRow, column);
				CellUpdate update = new CellUpdate(modelIndex, firstRow, column, valueAt);
				sendToAllClients(update);
			}
			else {
				sendToAllClients(copyModel(model));
			}
		}
	}

	private void sendToAllClients(Object obj) {
		synchronized (handlerList) {
			for (MySocketHandler handler : handlerList) {
				handler.writeObject(obj);
			}
		}
	}

	private class MySocketHandler {

		private ObjectOutputStream oos;

		public MySocketHandler(Socket socket) throws IOException {
			oos = new ObjectOutputStream(socket.getOutputStream());
			for (DefaultTableModel model : initialModelList) {
				writeObject(model);
			}
		}

		public void writeObject(Object obj) {
			try {
				oos.writeObject(obj);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
