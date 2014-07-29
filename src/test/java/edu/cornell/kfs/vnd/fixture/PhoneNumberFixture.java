package edu.cornell.kfs.vnd.fixture;

public enum PhoneNumberFixture {
	
	EIGHT_HUNNID("8001239876", "800-123-9876"),
	NYC("2124441234", "212-444-1234"),
	LOS_ANGELES("2139991212", "213-999-1212"),
	LONG_DISTANCE("13141592653", "1-314-159-2653");

	public final String formatted;
	public final String unformatted;
	
	private PhoneNumberFixture(String unformatted, String formatted) {
		this.unformatted = unformatted;
		this.formatted = formatted;		
	}
	
}
