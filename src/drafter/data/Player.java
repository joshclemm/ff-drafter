package drafter.data;

/**
 * 
 * @author Josh
 *
 */
public class Player {
	
	private int rank;
	private String name;
	private String team;
	private String pos;
	private String projectedPoints;
	private int byeWeek;
	
	private boolean hidden;
	
	public Player() {
		hidden = false;
	}

	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the team
	 */
	public String getTeam() {
		return team;
	}

	/**
	 * @param team the team to set
	 */
	public void setTeam(String team) {
		this.team = team;
	}

	/**
	 * @return the pos
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}

	/**
	 * @return the projectedPoints
	 */
	public String getProjectedPoints() {
		return projectedPoints;
	}

	/**
	 * @param projectedPoints the projectedPoints to set
	 */
	public void setProjectedPoints(String projectedPoints) {
		this.projectedPoints = projectedPoints;
	}
	
	public void setByeWeek(int byeWeek) {
		this.byeWeek = byeWeek;
	}
	
	public int getByeWeek() {
		return byeWeek;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public String toString() {
		return rank + " " + name + " " + team + " " + pos + " " + projectedPoints + " " + byeWeek;
	}
}
