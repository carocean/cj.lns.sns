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
	var chipon = frame.parameter('chip-on');
	var owner = frame.parameter('computer-owner');
	var chipmarketid = frame.parameter('chip-marketid');
	
	var site = plug.site();
	var netdisk = site.getService('netdisk');
	// print(netdisk + ' ' + computer);
	var home = netdisk.getLnsDataHome();
	var fs = home.fileSystem();
	
	var chip=getChip(home,chipmarketid);
	
	var homedir = site.getProperty('home.dir');
	var localDockersHome = getLocalDockersHome(new File(homedir));
	var destDir = String.format("%s%s%s%sneuron%sapps", localDockersHome, File.separator,
			owner,File.separator,File.separator);
	downloadChip(chip.tuple(),fs,destDir);
	saveInstallList(localDockersHome,owner,chipmarketid,chip.tuple());
}
//保存已安装的芯片列表
function saveInstallList(localDockersHome,owner,marketid,chip){
	var ifile=String.format("%s%s%s%sinstalltable.json", localDockersHome, File.separator,
			owner,File.separator);
	var json;
	var f=new File(ifile);
	if(!f.exists()){
		f.createNewFile();
	}
	var b=FileHelper.readFully(f);
	if(b==null||b.length<1){
		json="{}";
	}else{
		json=new String(b);
	}
	var map=new Gson().fromJson(json,HashMap.class);
	map.put(marketid,chip);
	json=new Gson().toJson(map);
	var out=null;
	try{
		out=new FileOutputStream(f);
		out.write(json.getBytes());
	}catch(e){
		throw new CircuitException('503',e);
	}finally{
		if(out!=null)
			out.close();
	}
}
function downloadChip(chip,fs,destDir){
	var srcfn=String.format("%s/%s",chip.market,chip.assembly);
	var f=fs.openFile(srcfn);
	var name=chip.assembly;
	name=name.substring(0,name.lastIndexOf('.jar'));
	var destfn=String.format("%s%s%s%s",destDir,File.separator,name,File.separator);
	var df=new File(destfn);
	if(!df.exists()){
		df.mkdir();
	}
	destfn=String.format("%s%s",destfn,chip.assembly);
	var reader = f.reader(0);
	var readlen = 0;
	var b = JavaUtil.createByteArray(8192);
	var out = null;
	try {

		out = new FileOutputStream(destfn);
		while ((readlen = reader.read(b, 0, b.length)) > -1) {
			out.write(b, 0, readlen);
		}
		//print(dfn);
		var dfile = new File(destfn);
		dfile.setExecutable(true);// 设置可执行权限
		dfile.setReadable(true);// 设置可读权限
		dfile.setWritable(true);// 设置可写权限
	} catch (e) {
		throw new CircuitException("503", e);
	} finally {
		if (out != null) {
			out.close();
		}
		reader.close();
		
	}
}
function getChip(home,chipmarketid){
	var cjql=String.format("select {'tuple':'*'} from tuple csc.market java.util.HashMap where {'_id':ObjectId('%s')}",chipmarketid)
	var q=home.createQuery(cjql);
	return q.getSingleResult();
}
function getLocalDockersHome(f) {
	if (f.getName() == 'dockers') {
		return f.getAbsolutePath();
	}
	return getLocalDockersHome(f.getParentFile());
}
