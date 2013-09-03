import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import com.googlecode.ipv6.*;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import java.math.BigInteger;

public class IPV6C extends JFrame implements ActionListener
{
    
    public JButton calcButton;
    public JTextField tf;
    public JPanel panel;
    public JLabel label;
    public JTextPane result;
    public IPV6C() {
	this.setTitle("IPv6 Clalculator");
        this.setSize(600, 400);
        panel = new JPanel();
	label = new JLabel("Enter IP/Subnetmask (e.g 192.168.42.1/255.255.255.0 or 2a00:42:42::1/56)");
	tf = new JTextField("2a00:42:42::1/56",40);
	tf.addActionListener(this);
	calcButton = new JButton("Calc");
	calcButton.addActionListener(this);
	result = new JTextPane();
	panel.add(label);
	panel.add(tf);
	panel.add(calcButton);
	panel.add(result);
        this.add(panel);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    } 
    public static void main(String[] args)
    {
	IPV6C i = new IPV6C();

        i.setVisible(true);
    }

    public void actionPerformed (ActionEvent ae){
        // Die Quelle wird mit getSource() abgefragt und mit den
        // Buttons abgeglichen. Wenn die Quelle des ActionEvents einer
        // der Buttons ist, wird der Text des JLabels entsprechend geändert
        if ((ae.getSource() == this.calcButton) || (ae.getSource() == this.tf)){
            result.setText("Button 1 wurde betätigt");
	    String val=tf.getText();
	    try {
		String [] valA=val.split("/");
		String ip=valA[0];
		String mask=valA[1];
		String output="";
		if (val.contains(".")) {
		    printIpv4(val,ip,mask);
		} else {
		    printIpv6(val,ip,mask);
		}
	    }
	    catch (ArrayIndexOutOfBoundsException e) {
		result.setText("Wrong format (No subnetmask?)");
	    }
	    catch (Exception e) {
		result.setText("Wrong format."+e.toString());
	    }

	} else {
	    result.setText("unknown event");
	}
    }
    public void printIpv4 ( String subnet, String ip, String mask) {
	String output="IPV4\n";
        SubnetUtils utils;
	if (mask.contains(".")) {
	    utils = new SubnetUtils(ip,mask);
	}  else {
	    utils = new SubnetUtils(subnet);
	}
        SubnetInfo info = utils.getInfo();

	output=output+"Network:        \t"+info.getNetworkAddress()+"\n"; 
	output=output+"First usable IP:\t"+info.getLowAddress()+"\n"; 
	output=output+"Last usable IP: \t"+info.getHighAddress()+"\n"; 
	output=output+"Broadcast:      \t"+info.getBroadcastAddress()+"\n"; 
	output=output+"CIDR Pefix:     \t"+info.getCidrSignature()+"\n"; 
	output=output+"Netmask:        \t"+info.getNetmask()+"\n"; 
	output=output+"Num. usable addresses:"+info.getAddressCount()+"\n";
	//	IPv6Address  adr= IPv6Address.fromString(ip);
	//output=output+"is IPv4 Mapped:  \t"+adr.isIPv4Mapped()+"\n";
	//	output=output+"is Site Local:  \t"+adr.isSiteLocal()+"\n";
	//	output=output+"is Link Local:  \t"+adr.isLinkLocal()+"\n";		

	// Reverse DNS

	result.setText(output);
    }

    public void printIpv6 ( String netVal, String ip, String mask) {
	IPv6Network net = IPv6Network.fromString(netVal);
	String output="IPV6\n";
	output=output+"Network:        \t"+net.getFirst().toLongString()+"/"+net.getNetmask().asPrefixLength()+"\n"; 
	if (net.getNetmask().asPrefixLength()<127) {
	    output=output+"First usable IP:\t"+net.getFirst().add(1).toLongString()+"\n"; 
	    output=output+"Last usable IP: \t"+net.getLast().add(-1).toLongString()+"\n"; 
	} else {
	    output=output+"!!Not usable net, choose greater netmask!!\n"; 
	}
	    output=output+"Broadcast:      \t"+net.getLast().toLongString()+"\n"; 
	    output=output+"CIDR Pefix:     \t"+net.getNetmask().asPrefixLength()+"\n"; 

	    output=output+"Netmask:        \t"+net.getNetmask().asAddress()+"\n"; 

	
	    BigInteger numberOfAddresses= new BigInteger("2").pow(128-net.getNetmask().asPrefixLength());
	    numberOfAddresses=numberOfAddresses.subtract(new BigInteger("2"));
	    output=output+"Num. of usable addresses:"+numberOfAddresses+"\n";


	if (net.getNetmask().asPrefixLength()<64) {
	    output=output+"\nFirst /64 Network:     \t"+net.getFirst().toLongString()+"/64\n"; 
	    
	    IPv6Network lastnet = IPv6Network.fromString(net.getLast().add(-1).toLongString()+"/64");
	    output=output+"Last /64 Network:      \t"+lastnet.getFirst().toLongString()+"/64\n"; 
	    BigInteger numberOfNetw= new BigInteger("2").pow(64-net.getNetmask().asPrefixLength());
	    output=output+"Number of /64 nets:\t"+numberOfNetw+"\n";
	}

	IPv6Address  adr= IPv6Address.fromString(ip);
	String adrS=adr.toLongString().replace(":","");
	int i,len=adrS.length();
	StringBuffer revDNS = new StringBuffer(70);
	StringBuffer revf64DNS = new StringBuffer(70);
	StringBuffer reve64DNS = new StringBuffer(70);
	for (i = (len - 1); i >= 0; i--) {
	    revDNS.append(adrS.charAt(i));
	    revDNS.append('.');
	    if (i<16) {
		revf64DNS.append(adrS.charAt(i));
		revf64DNS.append('.');
	    } else {
		reve64DNS.append(adrS.charAt(i));
		if (i>16) {
		    reve64DNS.append('.');
		}
	    }
	}

	output=output+"\nReverse DNS:     \t"+revDNS.toString()+"ip6.arpa.\n";
	output=output+"/64 DNS start:   \t"+revf64DNS.toString()+"ip6.arpa.\n";
	output=output+"/64 DNS end:     \t"+reve64DNS.toString()+"\n\n";
	output=output+"is IPv4 mapped:  \t"+adr.isIPv4Mapped()+"\n";
	output=output+"is Site local:   \t"+adr.isSiteLocal()+"\n";
	output=output+"is Link local:   \t"+adr.isLinkLocal()+"\n";		
	// FIXME MacAddress



	result.setText(output);
    }
}
