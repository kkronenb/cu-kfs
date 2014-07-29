package edu.cornell.kfs.vnd.service.impl;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.vnd.fixture.PhoneNumberFixture;

@ConfigureContext
public class CuPhoneNumberServiceImplTest extends KualiTestBase {

	CuPhoneNumberServiceImpl cuPhoneNumberServiceImpl;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cuPhoneNumberServiceImpl = SpringContext.getBean(CuPhoneNumberServiceImpl.class);
	}
	
	public void testFormatNumberIfPossible( ) {
		String phoneNumberOne = "5551239876";
		String phoneNumberTwo = "8001115000";
		String formattedNumberOne = cuPhoneNumberServiceImpl.formatNumberIfPossible(phoneNumberOne);
		String formattedNumberTwo = cuPhoneNumberServiceImpl.formatNumberIfPossible(phoneNumberTwo);
		
		
		PhoneNumberFixture oneEightHundred = PhoneNumberFixture.EIGHT_HUNNID;
		PhoneNumberFixture longDistance = PhoneNumberFixture.LONG_DISTANCE;
		PhoneNumberFixture losAngeles = PhoneNumberFixture.LOS_ANGELES;
		PhoneNumberFixture nyc = PhoneNumberFixture.NYC;
		
		System.out.println("formatted phone numbers: " + formattedNumberOne + " ** " +
		formattedNumberTwo);
		
		System.out.println("eight: " + oneEightHundred.unformatted + " ** " + cuPhoneNumberServiceImpl.formatNumberIfPossible(oneEightHundred.unformatted) + " ** FF: " + oneEightHundred.formatted);
		
		System.out.println("long distance: " + longDistance.unformatted + " ** " + cuPhoneNumberServiceImpl.formatNumberIfPossible(longDistance.unformatted) + " ** FF: " + longDistance.formatted);
	}
}
