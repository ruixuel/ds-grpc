package io.grpc.examples.helloworld;

import java.util.*;

public class IPList {
	private static Map<String, String> ipMap;
	private static IPList ipList = null;
	
	private IPList() {
		ipMap = new HashMap<String, String>();
	}
	
	public static IPList getInstance() {
		if(ipList == null) {
			ipList = new IPList();
		}
		return ipList;
	}
	
	public static void addIP(String ip, String username) {
		ipMap.put(ip, username);
	}
	
	public static void removeIP(String ip) {
		ipMap.remove(ip);
	}
	
	public static Map<String, String> getIPMap() {
		return ipMap;
	}
	
	public static String getName(String ip) {
		return ipMap.get(ip);
	}
	
	public static int getSize() {
		return ipMap.size();
	}
	
}
	
