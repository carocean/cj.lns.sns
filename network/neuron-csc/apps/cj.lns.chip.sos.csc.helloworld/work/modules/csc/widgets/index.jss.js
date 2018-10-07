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
var TupleDocument = Java.type('cj.lns.chip.sos.cube.framework.TupleDocument');

exports.flow=function(frame,circuit,plug,ctx){
	print('jss----test1');
	
	
	var visitor=ctx.visitor();
	var user='';
	var doc=null;
	if(visitor!=null){
		user=visitor.getVisitor();
		 doc = ctx.html("/index.html",
				"/?csc_resource_path=/"+user+"/helloworld/");
		var v=doc.select('.hello>p[visit]>span');
		v.html(visitor.getVisitor());
	}else{
		 doc = ctx.html("/index.html",
				"/");
	}
	
	circuit.content().writeBytes(doc.toString().getBytes());
}