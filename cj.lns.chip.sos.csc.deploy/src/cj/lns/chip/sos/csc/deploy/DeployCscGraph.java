package cj.lns.chip.sos.csc.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import cj.lns.chip.sos.csc.CscGraph;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.graph.GraphCreator;
@CjService(name="cj.neuron.app",isExoteric=true)
public class DeployCscGraph extends CscGraph{
	@CjServiceRef(refByName="netdisk")
	IDatabaseCloud db;
	@Override
	protected void build(GraphCreator c) {
		super.build(c);
		IServiceSite site=c.site();
		String homeDir = site.getProperty("home.dir");
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
