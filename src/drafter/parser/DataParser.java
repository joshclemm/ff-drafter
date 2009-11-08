package drafter.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import drafter.data.Player;

/**
 * This class provides the framework for parsing
 * fantasy football ranking information.  To parse
 * additional sources, this class should be subclassed.
 * 
 * @author Josh Clemm
 *
 */
public abstract class DataParser {

	protected ArrayList<Player> playerList;

	protected BufferedReader br;

	public DataParser(String filename) {
		playerList = new ArrayList<Player>();

		try {
			try {
				// look for file in the resource folder ('res')
				br = new BufferedReader(new FileReader("res"+File.separator+filename));
			} catch (FileNotFoundException e) {
				// look for file locally
				br = new BufferedReader(new FileReader(filename));
			}

		} catch (FileNotFoundException e) {
			System.err.println("Could not find rankings file");
		}

		parse();
	}

	protected boolean parse() {
		return false;
	}

	public Vector<Vector<String>> getData() {

		Comparator<Object> rankComparator = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				Player p1 = (Player) o1;
				Player p2 = (Player) o2;
				Integer d1 = Integer.valueOf(p1.getRank());
				Integer d2 = Integer.valueOf(p2.getRank());
				return d1.compareTo(d2);
			}
		};

		Collections.sort(playerList, rankComparator);

		Vector<Vector<String>> list = new Vector<Vector<String>>();
		for (Player player : playerList) {
			Vector<String> oneRow = new Vector<String>();
			oneRow.add(player.getRank()+"");
			oneRow.add(player.getName());
			oneRow.add(player.getPos());
			oneRow.add(player.getTeam());
			oneRow.add(player.getProjectedPoints());
			oneRow.add(player.getByeWeek()+"");
			oneRow.add(player.isHidden()+"");
			list.add(oneRow);
		}

		return list;
	}

}
