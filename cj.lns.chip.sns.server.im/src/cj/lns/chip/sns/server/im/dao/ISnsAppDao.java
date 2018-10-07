package cj.lns.chip.sns.server.im.dao;

import java.util.Map;

import cj.studio.ecm.graph.CircuitException;
import cj.studio.ecm.sns.mailbox.SnsApp;

public interface ISnsAppDao {
	SnsApp getApp(String appCode) throws CircuitException;
	Map<String,SnsApp> getAll();
}
