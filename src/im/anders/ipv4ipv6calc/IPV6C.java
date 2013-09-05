package im.anders.ipv4ipv6calc;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
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
        this.setSize(800, 400);
        panel = new JPanel();
	label = new JLabel("Enter IP/Subnetmask (e.g 192.168.42.1/255.255.255.0 or 2a00:42:42::1/56)");
	tf = new JTextField("2001:db8:42::1/56",40);

	tf.addActionListener(this);
	calcButton = new JButton("Calc");
	calcButton.addActionListener(this);
	result = new JTextPane();
	Font font=new Font("Monospaced", Font.PLAIN, 11); 
	result.setFont(font);
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
        if ((ae.getSource() == this.calcButton) || (ae.getSource() == this.tf)){
            result.setText("");
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

	output=output+"Network:        \t"+info.getNetworkAddress()+"\t["+Integer.toBinaryString(info.asInteger(info.getNetworkAddress()))+"]\n"; 
	output=output+"First usable IP:\t"+info.getLowAddress()+"\t["+Integer.toBinaryString(info.asInteger(info.getLowAddress()))+"]\n"; 
	output=output+"Last usable IP: \t"+info.getHighAddress()+"\t["+Integer.toBinaryString(info.asInteger(info.getHighAddress()))+"]\n"; 
	output=output+"Broadcast:      \t"+info.getBroadcastAddress()+"\t["+Integer.toBinaryString(info.asInteger(info.getBroadcastAddress()))+"]\n"; 
	output=output+"CIDR Pefix:     \t"+info.getCidrSignature()+"\n"; 
	output=output+"Netmask:        \t"+info.getNetmask()+"\t["+Integer.toBinaryString(info.asInteger(info.getNetmask()))+"]\n"; 
	output=output+"Num. usable addresses:"+info.getAddressCount()+"\n";
	//	IPv6Address  adr= IPv6Address.fromString(ip);
	//output=output+"is IPv4 Mapped:  \t"+adr.isIPv4Mapped()+"\n";
	//	output=output+"is Site Local:  \t"+adr.isSiteLocal()+"\n";
	//	output=output+"is Link Local:  \t"+adr.isLinkLocal()+"\n";		

	// Reverse DNS

	result.setText(output);
    }

    public  byte hexToByte(String s) {
	byte b=(byte) ((Character.digit(s.charAt(0), 16) << 4)
		       + Character.digit(s.charAt(1), 16));
	return b;
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
	    output=output+"\nFirst /64 Network: \t"+net.getFirst().toLongString()+"/64\n"; 
	    
	    IPv6Network lastnet = IPv6Network.fromString(net.getLast().add(-1).toLongString()+"/64");
	    output=output+"Last /64 Network:  \t"+lastnet.getFirst().toLongString()+"/64\n"; 
	    BigInteger numberOfNetw= new BigInteger("2").pow(64-net.getNetmask().asPrefixLength());
	    output=output+"Number of /64 nets:\t"+numberOfNetw+"\n";
	} 
	if (net.getNetmask().asPrefixLength()>64) {
	    output=output+"\nWarning: For stateless address autoconfiguration (SLAAC) subnets\n      require a /64 address block. See RFC 4291 section 2.5.1.\n";
	}
	IPv6Address  adr= IPv6Address.fromString(ip);
	String adrS=adr.toLongString().replace(":","");
	output=output+"\n";
	if ((adrS.substring(22,24).equals("ff")) &&
	    (adrS.substring(24,26).equals("fe"))) {
	    int b= hexToByte(adrS.substring(16,18));
	    b=b^2;	
	    String fs=Integer.toHexString(b);
	    if (fs.length()==1) {
		fs="0"+fs;
	    }
	   
	    output=output+"EUI-48/MAC address:\t"+
		fs+":"+
		adrS.substring(18,20)+":"+
		adrS.substring(20,22)+":"+
		adrS.substring(26,28)+":"+
		adrS.substring(28,30)+":"+
		adrS.substring(30,32)+"\n";
	}
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

	output=output+"Reverse DNS:     \t"+revDNS.toString()+"ip6.arpa.\n";
	output=output+"/64 DNS start:   \t"+revf64DNS.toString()+"ip6.arpa.\n";
	output=output+"/64 DNS end:     \t"+reve64DNS.toString()+"\n\n";
	if (adr.isIPv4Mapped())
	    output=output+"The Address is IPv4 mapped.\n";
	if (adr.isSiteLocal()) 
	    output=output+"The Address is site local.\n";
	if (adr.isLinkLocal())
	    output=output+"The Address is Link local.\n";
	
	if (IPv6Network.fromString("2000::/3").contains(adr)) 
	    output=output+"The Address is in global-unicast network.\n";

	if (IPv6Network.fromString("2001::/32").contains(adr)) 
	    output=output+"Teredo network address.\n";

	if (IPv6Network.fromString("2001:db8::/32").contains(adr)) 
	    output=output+"Dokumentation address. Do not use in real life.\n";

	if (IPv6Network.fromString("64:ff9b::/96").contains(adr))
	    output=output+"The Address is in NAT64 space. See RFC 6146\n";

		

	result.setText(output);
    }
}
