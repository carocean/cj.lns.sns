/*
 * 说明：
 * 作者：extends可以实现一种类型，此类型将可在java中通过调用服务提供器的.getServices(type)获取到。
 * <![jss:{
		scope:'runtime'
 	}
 ]>
 <![desc:{
	ttt:'2323',
	obj:{
		name:'09skdkdk'
		}
 * }]>
 */
//var imports = new JavaImporter(java.io, java.lang)导入类型的范围，单个用Java.type
var Frame = Java.type('cj.studio.ecm.frame.Frame');
var FormData = Java.type('cj.studio.ecm.frame.FormData');
var FieldData = Java.type('cj.studio.ecm.frame.FieldData');
var Circuit = Java.type('cj.studio.ecm.frame.Circuit');
var String = Java.type('java.lang.String');
var CircuitException = Java.type('cj.studio.ecm.graph.CircuitException');
var Gson = Java.type('cj.ultimate.gson2.com.google.gson.Gson');
var StringUtil = Java.type('cj.ultimate.util.StringUtil');
var Document = Java.type('org.jsoup.nodes.Document');
var Jsoup = Java.type('org.jsoup.Jsoup');
var System = Java.type('java.lang.System');
var HashMap = Java.type('java.util.HashMap');
var File = Java.type('java.io.File');
var FileOutputStream = Java.type('java.io.FileOutputStream');
var TupleDocument = Java.type('cj.lns.chip.sos.cube.framework.TupleDocument');
var JavaUtil = Java.type('cj.ultimate.util.JavaUtil');
var FileWriter = Java.type('java.io.FileWriter');
var BufferedWriter = Java.type('java.io.BufferedWriter');
var FileHelper = Java.type('cj.ultimate.util.FileHelper');

exports.flow = function(frame, circuit, plug, ctx) {
	var visitor = ctx.visitor();
	var deployer = frame.parameter('deployer');
	print(String.format('开始为：%s 部署云计算机...',deployer));
	var json = new String(frame.content().readFully());
	var computer = new Gson().fromJson(json, HashMap.class);
	var site = plug.site();
	var netdisk = site.getService('netdisk');
	// print(netdisk + ' ' + computer);
	var home = netdisk.getLnsDataHome();
	var fs = home.fileSystem();
	var path = String.format("/dockers/%s", computer.docker);
	var dir = fs.dir(path);
	var homedir = site.getProperty('home.dir');
	var localDockersHome = getLocalDockersHome(new File(homedir));

	var destDir = String.format("%s%s%s", localDockersHome, File.separator,
			deployer);
	// print(path);
	// print(destDir);
	// 下载
	exportsDocker(fs, dir, new File(destDir));
	// 生成启动文件
	genStartScriptFile(localDockersHome, deployer, computer);
	// 修改容器内神经元协议和端口
	editNeuronServer(localDockersHome, deployer, computer);
	// 部署完毕
	print(String.format('部署%s完毕。云计算机信息：\r\n\t%s',deployer,computer));
}
function editNeuronServer(localDockersHome, deployer, computer){
	var serverjson=String.format("%s%s%s%sneuron%sconf%sservers.json",localDockersHome,File.separator,deployer,File.separator,File.separator,File.separator);
	var file=new File(serverjson);
	var b=FileHelper.readFully(file);
	var json=new String(b);
	var map=new Gson().fromJson(json,HashMap.class);
	var ct=computer.cscCustomer;
	var pt=ct.substring(0,ct.indexOf('://'));
	var port=computer.nrport;
	map.website.protocol=pt;
	map.website.port=port;
	json=new Gson().toJson(map);
	var out = null;
	var writer = null;
	try {
		out = new FileWriter(file);
		writer = new BufferedWriter(out);
		writer.write(json);
		writer.newLine();
	} catch (e) {
		throw new CircuitException("503", e);
	} finally {
		if (writer != null) {
			writer.close();
		}
		if (out != null) {
			out.close();
		}
		
		file.setExecutable(true);// 设置可执行权限
		file.setReadable(true);// 设置可读权限
		file.setWritable(true);// 设置可写权限
	}
}
function genStartScriptFile(localDockersHome, deployer, computer) {
	var sfile = String.format("%s%sstart-%s.sh", localDockersHome,
			File.separator, deployer);
	var f = new File(sfile);
	if (!f.exists()) {
		f.createNewFile();
	}
	var out = null;
	var writer = null;
	try {
		out = new FileWriter(sfile);
		writer = new BufferedWriter(out);
		var ct = computer.cscCustomer;
		var port = ct.substring(ct.lastIndexOf(':') + 1, ct.length);
		var script = String
				.format(
						"gnome-terminal -x bash -c 'docker run -p %s:%s --rm -i -t --name %s -v $(pwd)/%s:/home/cj/studio %s env LANG=zh_CN.UTF-8 /bin/bash'",
						port, computer.nrport,deployer,deployer, computer.docker);
		writer.write(script);
		writer.newLine();
	} catch (e) {
		throw new CircuitException("503", e);
	} finally {
		if (writer != null) {
			writer.close();
		}
		if (out != null) {
			out.close();
		}
		var dfile = new File(sfile);
		dfile.setExecutable(true);// 设置可执行权限
		dfile.setReadable(true);// 设置可读权限
		dfile.setWritable(true);// 设置可写权限
	}

}

function getLocalDockersHome(f) {
	if (f.getName() == 'dockers') {
		return f.getAbsolutePath();
	}
	return getLocalDockersHome(f.getParentFile());
}
function exportsDocker(fs, dir, destDir) {
	if (!destDir.exists()) {
		destDir.mkdirs();
		destDir.setExecutable(true);// 设置可执行权限
		destDir.setReadable(true);// 设置可读权限
		destDir.setWritable(true);// 设置可写权限
	}

	var files = dir.listFiles();
	for (var i = 0; i < files.size(); i++) {
		var f = files.get(i);
		var destfn = f.name();
		var dfn = String.format("%s%s%s", destDir.getAbsolutePath(),
				File.separator, destfn);

		var reader = f.reader(0);
		var readlen = 0;
		var b = JavaUtil.createByteArray(8192);
		var out = null;
		try {

			out = new FileOutputStream(dfn);
			while ((readlen = reader.read(b, 0, b.length)) > -1) {
				out.write(b, 0, readlen);
			}
			//print(dfn);
		} catch (e) {
			throw new CircuitException("503", e);
		} finally {
			if (out != null) {
				out.close();
			}
			reader.close();
			var dfile = new File(dfn);
			dfile.setExecutable(true);// 设置可执行权限
			dfile.setReadable(true);// 设置可读权限
			dfile.setWritable(true);// 设置可写权限
		}
	}
	var dirs = dir.listDirs();
	for (var i = 0; i < dirs.size(); i++) {
		var d = dirs.get(i);
		var dest = new File(String.format("%s%s%s", destDir.getAbsolutePath(),
				File.separator, d.dirName()));

		exportsDocker(fs, d, dest)
	}
}