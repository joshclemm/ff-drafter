package drafter.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import drafter.data.Player;

/**
 * This class will parse the Fantasy football toolkit
 * rankings from year 2009.  May need to be changed
 * for future years.
 * 
 * @author Josh
 */
public class FFTPosDataParser extends DataParser {

	public FFTPosDataParser() {
		super("fftoolkit_pos_rankings.txt");
	}

	protected boolean parse() {
		try {
			String line = null;
			String pos = null;

			while((line = br.readLine()) != null) {

				if(line.startsWith("----")) {
					pos = line.split("\\s+")[1].trim();
				}
				else {
					playerList.add(createPlayerFromLine(line,pos));
				}
			}
			
			//re-adjust rankings based on projected points
			Comparator<Object> projPointsComparator = new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Player p1 = (Player) o1;
					Player p2 = (Player) o2;
					
					Double d1 = Double.valueOf(p1.getProjectedPoints());
					Double d2 = Double.valueOf(p2.getProjectedPoints());
					
					//apply any weights
					d1 = applyWeights(p1, d1);
					d2 = applyWeights(p2, d2);
					
					return d2.compareTo(d1);
				}

				private Double applyWeights(Player p1, Double d1) {
					if(p1.getPos().equals("QB")) {
						d1 *= 2.9;
					}
					else if(p1.getPos().equals("RB")) {
						d1 *= 4.5;
					}
					else if(p1.getPos().equals("WR")) {
						d1 *= 4.2;
					}
					else if(p1.getPos().equals("TE")) {
						d1 *= 4.5;
					}
					else if(p1.getPos().equals("DEF")) {
						d1 *= 3.5;
					}
					else if(p1.getPos().equals("K")) {
						d1 *= 2.45;
					}
					return d1;
				}
			};

			Collections.sort(playerList, projPointsComparator);
			
			//now reset ranks
			int rank = 1;
			for (Player player : playerList) {
				player.setRank(rank++);
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

	private Player createPlayerFromLine(String line, String pos) {
		Player player = new Player();
		line = line.trim();

		String[] tokens = line.split("\\s+");
		int rank = Integer.parseInt(tokens[0].trim());
		player.setRank(rank);

		try {

			int byeWeek = Integer.parseInt(tokens[4].trim());
			player.setByeWeek(byeWeek);
			
			String name = tokens[1].trim()+ " " + tokens[2].trim();
			player.setName(name);

			String team = tokens[3].trim().toUpperCase();
			player.setTeam(team);

		} catch (NumberFormatException e) {
			// Player has more than 2 words in name, so adjust index
			
			String name = tokens[1].trim()+ " " + tokens[2].trim() + " " + tokens[3].trim();
			player.setName(name);
			
			String team = tokens[4].trim().toUpperCase();
			player.setTeam(team);
			
			int byeWeek = Integer.parseInt(tokens[5].trim());
			player.setByeWeek(byeWeek);

		}

		String projectedPoints = tokens[tokens.length-1].trim();
		player.setProjectedPoints(projectedPoints);

		player.setPos(pos);
		
		// remove Defense from name
		if(player.getName().contains("Defense")) {
			player.setName(player.getName().replaceAll("Defense", "").trim());
		}

		return player;
	}
}
