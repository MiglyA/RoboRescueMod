package anget.ambulanceteam;

import anget.StandardAgent;

public class AT extends StandardAgent {
	public int hp;
	public String team_name;

	public AT(int entityID, int spaen_locationID) {
		super(entityID, spaen_locationID);
		team_name = "ambulanceteam";
	}
}
