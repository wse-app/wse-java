package wse.utils.json2;

import java.util.Collection;
import java.util.LinkedList;

public class JStringBuilder extends LinkedList<String> {
	private static final long serialVersionUID = 9192745881498526157L;
	
	private int length = 0;
	
	public int length() {
		return length;
	}
	
	@Override
	public boolean add(String e) {
		if (e == null || e.isEmpty()) return false;
		length += e.length();
		return super.add(e);
	}
	
	@Override
	public void add(int index, String e) {
		if (e == null || e.isEmpty()) return;
		length += e.length();
		super.add(index, e);
	}
	
	@Override
	public boolean addAll(Collection<? extends String> c) {
		for (String s : c) length += s.length();
		return super.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends String> c) {
		for (String s : c) length += s.length();
		return super.addAll(index, c);
	}
	
	@Override
	public void addFirst(String e) {
		if (e == null || e.isEmpty()) return;
		length += e.length();
		super.addFirst(e);
	}
	
	@Override
	public void addLast(String e) {
		if (e == null || e.isEmpty()) return;
		length += e.length();
		super.addLast(e);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(length());
		
		for (String s : this) {
			builder.append(s);
		}
		
		return builder.toString();
	}
	
	
}
