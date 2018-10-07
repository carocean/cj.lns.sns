package cj.lns.chip.sns.server.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import cj.lns.chip.sns.server.fs.cache.IPeerCache;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.graph.GraphCreator;
import cj.studio.ecm.graph.IPin;
import cj.studio.ecm.graph.IPinOptionsEvent;
import cj.studio.ecm.sns.SnsGraph;

@CjService(name = "cj.neuron.app", isExoteric = true)
public class FsGraph extends SnsGraph {
	@CjServiceRef(refByName = "databaseCloud")
	IDatabaseCloud db;

	@Override
	protected String defineAcceptProptocol() {
		return null;
	}

	@Override
	protected GraphCreator newCreator() {
		return new FsGraphCreator();
	}

	@Override
	protected void build(GraphCreator c) {
		super.build(c);
		IPin inputDevice = inputTerminus();
		inputDevice.plugFirst("authSink", c.newSink("authSink"));
		
		String homeDir = c.site().getProperty("home.dir");
		initDatabaseCloud(homeDir);
		
		IPin inputCloud=inputCloud();
		inputCloud.setOptionsEvent(new IPinOptionsEvent() {
			
			@Override
			public void onPut(String key, Object value) {
				if("bind-address".equals(key)){
					//第一个用户上线时检查是否清理过系统
					IPeerCache cache=(IPeerCache)c.site().getService("peerCache");
					cache.setOnAddress((String)value);
					cache.close();
				}
				
			}
		});
	}


	private void initDatabaseCloud(String homeDir) {
		String fileName = String.format("%s%sconfig%sdatabase-cloud.properties",
				homeDir, File.separator, File.separator);
		File f = new File(fileName);
		if (!f.exists()) {
			throw new EcmException(
					String.format("lns云数据库未配置，应在应用启动目录下建立文件:%s", f));
		}
		Properties props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			props.load(in);
		} catch (IOException e) {
			throw new EcmException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		db.init(props);
	}

}
