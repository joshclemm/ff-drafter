package drafter.parser;

import java.io.FileNotFoundException;
import java.io.IOException;

import drafter.data.Player;

/**
 * This class will parse the ESPN fantasy football
 * rankings from year 2009.  May need to be changed
 * for future years.
 * 
 * @author Josh
 */
public class ESPNDataParser extends DataParser {

	public ESPNDataParser() {
		super("espn_rankings.txt");
	}

	protected boolean parse() {
		try {
			String line = null;

			while((line = br.readLine()) != null) {
				playerList.add(createPlayerFromLine(line));
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

	private Player createPlayerFromLine(String line) {
		Player player = new Player();
		line = line.trim();

		String[] commaTokens = line.split(",");
		String nameRank = commaTokens[0];
		String[] nameRankTokens = nameRank.split("\\s+");
		int rank = Integer.parseInt(nameRankTokens[0]);
		player.setRank(rank);
		String name = nameRank.replaceAll(nameRankTokens[0], "").trim().replace("*", "");
		player.setName(name);

		String statsTokens = commaTokens[1].trim();
		String[] statsSpaceTokens = statsTokens.split("\\s+");
		String team = statsSpaceTokens[0].trim().toUpperCase();
		player.setTeam(team);
		String pos = statsSpaceTokens[1].trim();
		
		if(pos.contains("D/ST")) {
			player.setPos("DEF");
		}
		else {
			player.setPos(pos);
		}
		
		//remove middle initials
		if(player.getName().contains("Roy E. Williams")) {
			player.setName("Roy Williams");
		}
		
		String projectedPoints = statsSpaceTokens[statsSpaceTokens.length-1].trim();
		player.setProjectedPoints(projectedPoints);

		return player;
	}
}
