package proiect_licenta.planner.execution.instance_factory;

import proiect_licenta.planner.execution.ec2_instance.KeyPairWrapper;
import proiect_licenta.planner.execution.ec2_instance.SecurityGroupWrapper;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.util.logging.Logger;

abstract class InstanceFactoryAbs implements InstanceFactory {
	public InstanceFactoryAbs(Ec2Client client, InstanceType instanceType, String ami, String userData) {
		this.client = client;
		this.instanceType = instanceType;
		this.ami = ami;
		this.userData = userData;
		this.securityGroupWrapper = new SecurityGroupWrapper(client, "sg", "sg");
		this.keyPairWrapper = new KeyPairWrapper(client, "kp", "rsa");

		securityGroupWrapper.authorizeIngress(Helper.myIP());
		securityGroupWrapper.authorizeAll();
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
