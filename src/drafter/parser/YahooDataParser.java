package drafter.parser;

import java.io.FileNotFoundException;
import java.io.IOException;

import drafter.data.Player;

/**
 * This class will parse the Yahoo fantasy football
 * rankings from year 2009.  May need to be changed
 * for future years.
 * 
 * @author Josh
 */
public class YahooDataParser extends DataParser {

	public YahooDataParser() {
		super("yahoo_rankings.txt");
	}

	protected boolean parse() {
		try {

			String line = null;

			Player player = null;

			int lineReadCount = 0;
			while((line = br.readLine()) != null) {
				lineReadCount++;
				switch (lineReadCount) {
				case 1:
					player = new Player();
					player.setName(line.trim());
					break;
				case 2:
					setTeamAndPosition(player,line.trim());
					break;
				case 3:
					setRankAndPoints(player,line.trim());
					lineReadCount = 0;
					playerList.add(player);
					break;
				default:
					break;
				}
			}

			return true;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find rankings file");
		} catch (IOException e) {
			System.err.println("Could not read file");
			e.printStackTrace();
		}
		return false;
	}

	private void setRankAndPoints(Player player, String trim) {
		if(player != null) {
			String[] tokens = trim.split("FA");
			String[] split = tokens[1].trim().split("\\s+");
			player.setRank(Integer.parseInt(split[0].trim()));
			player.setProjectedPoints(split[split.length-1].trim());
		}
	}

	private void setTeamAndPosition(Player player, String trim) {
		if(player != null) {
			trim = trim.replace("(", "");
			trim = trim.replace(")","");
			String[] tokens = trim.split("-");
			player.setTeam(tokens[0].trim().toUpperCase());
			player.setPos(tokens[1].replace("NA", "").trim());
		}
	}
}
