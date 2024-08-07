package proiect_licenta.planner.execution.ec2_instance.instance_factory;

import proiect_licenta.planner.execution.ec2_instance.KeyPairWrapper;
import proiect_licenta.planner.execution.ec2_instance.SecurityGroupWrapper;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.util.HashMap;
import java.util.Map;

abstract class InstanceFactoryAbs implements InstanceFactory {


	private static final Map<Ec2Client, SecurityGroupWrapper> securityGroupWrappers = new HashMap<>();
	private static final Map<Ec2Client, KeyPairWrapper> keyPairWrappers = new HashMap<>();


	private static SecurityGroupWrapper getSecurityGroupWrapper(Ec2Client client) {
		SecurityGroupWrapper securityGroupWrapper = securityGroupWrappers.get(client);
		if (securityGroupWrapper == null) {
			securityGroupWrapper = new SecurityGroupWrapper(client, "sg", "sg");
			securityGroupWrapper.authorizeIngress(Helper.myIP());
			securityGroupWrapper.authorizeAll();
			securityGroupWrappers.put(client, securityGroupWrapper);
		}
		return securityGroupWrapper;
	}

	private static KeyPairWrapper getKeyPairWrapper(Ec2Client client) {
		KeyPairWrapper keyPairWrapper = keyPairWrappers.get(client);
		if (keyPairWrapper == null) {
			keyPairWrapper = new KeyPairWrapper(client, "kp", "rsa");
			keyPairWrappers.put(client, keyPairWrapper);
		}
		return keyPairWrapper;
	}

	public InstanceFactoryAbs(Ec2Client client, InstanceType instanceType, String ami, String userData) {
		this.client = client;
		this.instanceType = instanceType;
		this.ami = ami;
		this.userData = userData;
		this.securityGroupWrapper = getSecurityGroupWrapper(client);
		this.keyPairWrapper = getKeyPairWrapper(client);


	}


	protected final Ec2Client client;
	protected final SecurityGroupWrapper securityGroupWrapper;
	protected final KeyPairWrapper keyPairWrapper;
	protected final String ami;
	protected final String userData;
	protected final InstanceType instanceType;


	@Override
	public void clean() {
		keyPairWrapper.delete();
	}
}
