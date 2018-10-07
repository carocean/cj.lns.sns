package cj.lns.chip.sns.server.device.cde;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TerminusReporter implements Iterable<TerminusMessage>{
	String sid;
	private List<TerminusMessage> messages;
	public TerminusReporter() {
		messages=new ArrayList<>();
	}
	public TerminusReporter(String sid) {
		this();
		this.sid=sid;
	}
	public void add(TerminusMessage msg){
		messages.add(msg);
	}
	public int count(){
		return messages.size();
	}
	public void remove(TerminusMessage msg){
		 messages.remove(msg);
	}
	@Override
	public Iterator<TerminusMessage> iterator() {
		return messages.iterator();
	}
	
}
