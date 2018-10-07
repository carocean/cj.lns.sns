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
	var owner = frame.parameter('computer-owner');
	
	var site = plug.site();
	var homedir = site.getProperty('home.dir');
	var localDockersHome = getLocalDockersHome(new File(homedir));
	var json=getInstallList(localDockersHome,owner);
	
	circuit.content().writeBytes(json.getBytes());
}
//保存已安装的芯片列表
function getInstallList(localDockersHome,owner){
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
	return json;
}
function getLocalDockersHome(f) {
	if (f.getName() == 'dockers') {
		return f.getAbsolutePath();
	}
	return getLocalDockersHome(f.getParentFile());
}
