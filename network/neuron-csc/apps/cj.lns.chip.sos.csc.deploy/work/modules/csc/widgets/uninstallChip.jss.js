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
var Runtime = Java.type('java.lang.Runtime');
var HashMap = Java.type('java.util.HashMap');
var File = Java.type('java.io.File');
var FileOutputStream = Java.type('java.io.FileOutputStream');
var TupleDocument = Java.type('cj.lns.chip.sos.cube.framework.TupleDocument');
var JavaUtil = Java.type('cj.ultimate.util.JavaUtil');
var FileWriter = Java.type('java.io.FileWriter');
var BufferedWriter = Java.type('java.io.BufferedWriter');
var BufferedReader = Java.type('java.io.BufferedReader');
var InputStreamReader = Java.type('java.io.InputStreamReader');
var FileHelper = Java.type('cj.ultimate.util.FileHelper');
//原理以容器名停止容器：docker stop user1 说明：user1是计算机名即docker容器的名字
exports.flow = function(frame, circuit, plug, ctx) {
	var chipon = frame.parameter('chip-on');
	var owner = frame.parameter('computer-owner');
	var chipmarketid = frame.parameter('chip-marketid');
	
	var site = plug.site();
	
	var homedir = site.getProperty('home.dir');
	var localDockersHome = getLocalDockersHome(new File(homedir));
	var destDir = String.format("%s%s%s%sneuron%sapps", localDockersHome, File.separator,
			owner,File.separator,File.separator);
	saveInstallList(localDockersHome,owner,chipmarketid,destDir);
}
function saveInstallList(localDockersHome,owner,chipmarketid,destDir){
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
	if(map.containsKey(chipmarketid)){
		var chip=map.get(chipmarketid);
		var name=chip.assembly;
		name=name.substring(0,name.lastIndexOf('.jar'));
		var destfn=String.format("%s%s%s%s",destDir,File.separator,name,File.separator);
		var df=new File(destfn);
		if(df.exists()){
			FileHelper.deleteDir(df);
			//print(destfn);
		}
		
		map.remove(chipmarketid);
	}
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
function getLocalDockersHome(f) {
	if (f.getName() == 'dockers') {
		return f.getAbsolutePath();
	}
	return getLocalDockersHome(f.getParentFile());
}