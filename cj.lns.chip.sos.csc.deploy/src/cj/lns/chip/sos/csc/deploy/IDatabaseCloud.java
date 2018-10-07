package cj.lns.chip.sos.csc.deploy;

import java.util.Properties;

import com.mongodb.MongoClient;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.disk.INetDisk;
import cj.ultimate.IClosable;
/**
 * 提供三个数据云的连接
 * <pre>
 * 1.lns系统结构性数据云
 * 2.lns系统基础数据性数据云
 * 3.用户数据云
 * </pre>
 * @author carocean
 *
 */
public interface IDatabaseCloud extends IClosable{
	MongoClient userClient();
	/**
	 * lns系统磁盘的主存空间
	 * <pre>
	 *
	 * </pre>
	 * @return
	 */
	ICube getLnsSysHome();


	void init(Properties props);


	INetDisk getUserDisk(String user);


	INetDisk getLnsDataDisk();


	ICube getLnsDataHome();
	INetDisk getLnsSysDisk();
	
}
