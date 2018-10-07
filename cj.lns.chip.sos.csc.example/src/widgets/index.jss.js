/*
 * 功能：演示
 * 版本：1.0
 * 作者：cj
 * extends可以实现一种类型，此类型将可在java中通过调用服务提供器的.getServices(type)获取到。这样在java代码中直接使用接口间接的调用到jss实现
 * 注意使用extends的限制：
 * 1.jss必须实现该接口的方法，而且一定是导出方法，即声明为exports.method=function格式
 * 如果未有正确实现extends接口方法，则返回为null
 * 
 * extends的调用参考RefJssService类中的用例
 * 
 * 芯片内服务有三种：
 * －－ tool：工具，位于服务台发送按钮下的工具栏，由工具绑定，提供表单服务
 * －－ site:站点，即第三方web3.0网站（第三方开发），服务菜单绑定，点击采单将跳转到站点
 * －－ bflow：业务流程，提供功能性、流程性、交互性服务，由第三方开发。
 * <![jss:{
		scope:'runtime',
		extends:'cj.lns.chip.sos.csc.ICsc'
 	}
 ]>
 <![csc:{
	type:'tool',
	title:'测试',
	usage:'用法'
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

exports.definition=function(){
	return imports;
}

exports.flow=function(frame,circuit,plug,ctx){
	print('example----'+frame+'----'+imports);
	
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