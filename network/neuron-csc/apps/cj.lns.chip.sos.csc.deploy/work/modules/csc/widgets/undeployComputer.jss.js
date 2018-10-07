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
var FileUtils = Java.type('org.apache.commons.io.FileUtils');
//原理以容器名停止容器：docker stop user1 说明：user1是计算机名即docker容器的名字
exports.flow = function(frame, circuit, plug, ctx) {
	var owner = frame.parameter('computer-owner');
	print(String.format('正在销毁：%s 的计算机',owner))
	var site=plug.site();
	var stop=site.getService('$.cj.jss.csc.stopComputer');
	try{
		stop.flow(frame,circuit,plug,ctx);
	}catch(e){
		
	}
	
	var homedir = site.getProperty('home.dir');
	var localDockersHome = getLocalDockersHome(new File(homedir));
	var pchome=new File(String.format("%s%s%s",localDockersHome,File.separator,owner));
	//FileHelper.deleteDir(pchome);
	try{
	FileUtils.forceDelete(pchome);
	}catch(e){
		//静默异常
	}finally{//不管用户计算机目录删除成功不成功都清理计算机
		var startFile=new File(String.format("%s%sstart-%s.sh",localDockersHome,File.separator,owner));
		FileUtils.forceDelete(startFile);
		print(String.format('已销毁：%s 的计算机',owner));
	}
	
}
function getLocalDockersHome(f) {
	if (f.getName() == 'dockers') {
		return f.getAbsolutePath();
	}
	return getLocalDockersHome(f.getParentFile());
}