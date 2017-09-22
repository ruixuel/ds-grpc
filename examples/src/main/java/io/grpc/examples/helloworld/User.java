package io.grpc.examples.helloworld;

import java.util.*;

public class User {
	private String name;
	private String ip;
	
	public User(String name, String ip) {
		this.name = name;
		this.ip = ip;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIP() {
		return ip;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(!(o instanceof User)) {
			return false;
		}
		User u = (User) o;
		return u.getName().equals(this.getName()) && u.getIP().equals(this.getIP());
	}
	
}