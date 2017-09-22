package io.grpc.examples.helloworld;

import java.util.*;

public class IPList {
	private static List<User> userList;
	private static Set<String> ips;
	private static IPList ipList = null;
	
	private IPList() {
		userList = new ArrayList<User>();
		ips = new HashSet<String>();
	}
	
	public static IPList getInstance() {
		if(ipList == null) {
			ipList = new IPList();
		}
		return ipList;
	}
	
	public static void addUser(String ip, String username) {
		if(ips.contains(ip)) {
			return;
		}
		User u = new User(username, ip);
		userList.add(u);
		ips.add(ip);
	}
	
	public static void removeUser(String name, String ip) {
		ips.remove(ip);
		userList.remove(new User(name, ip));		
	}
	
	public static User getUserByIndex(int i) {
		return userList.get(i);
	}
	
	public static int getSize() {
		return userList.size();
	}
	
}
	
