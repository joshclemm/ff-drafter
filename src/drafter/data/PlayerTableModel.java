package drafter.data;

import java.util.HashMap;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

public class PlayerTableModel extends DefaultListModel {

	private static final long serialVersionUID = 1L;

	private HashMap<String, String> playerMap;

	public PlayerTableModel() {
		playerMap = new HashMap<String, String>();
	}

	public void addPlayer(Player player) {
		if(!playerMap.containsKey(player.getName())) {
			String entry = player.getPos()+" - "+player.getName();
			playerMap.put(player.getName(), entry);
			addElement(entry);
		}
	}

	public void removePlayer(String name) {
		if(playerMap.containsKey(name)) {
			String remove = playerMap.remove(name);
			removeElement(remove);
		}
	}

	@Override
	public Object remove(int index) {
		Object object = super.get(index);
		Set<String> keySet = playerMap.keySet();
		for (String string : keySet) {
			String value = playerMap.get(string);
			if(value.equals(object)) {
				playerMap.remove(string);
				return super.remove(index);
			}
		}
		return null;
	}

	public void update(TableModelEvent e) {
		Object source = e.getSource();

		if(source instanceof DefaultTableModel) {
			DefaultTableModel model = (DefaultTableModel) source;

			int nameColumn = model.findColumn("Name");

			String name = (String) model.getValueAt(e.getFirstRow(), nameColumn);
			removePlayer(name);
		}
	}

}
