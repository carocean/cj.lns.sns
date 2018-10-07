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
var HashMap = Java.type('java.util.HashMap');
var ArrayList = Java.type('java.util.ArrayList');
var File = Java.type('java.io.File');
//var InputServerConnectArgs = Java.type('cj.lns.chip.sns.neuron.core.app.InputServerConnectArgs');

exports.flow=function(frame,circuit,plug,ctx){
	var chip=new Gson().fromJson(new String(frame.content().readFully()),HashMap.class);
	//print(chip);
	var site=plug.site();
	var appManager=site.getService('neuronAppManager');
	var neuron=appManager.getNeuron();
	
	var visitor=ctx.visitor();
	var ac=neuron.appContainer;
	if(!ac.containsApp(chip.name)){
		var name=chip.assembly;
		name=name.substring(0,name.lastIndexOf('.jar'));
		
		var fn=String.format("%s%s%s%s%s",ac.getPluginDir(),File.separator,name,File.separator,chip.assembly);
		var file=new File(fn);
		var list=new ArrayList();
		ac.loadAssembly(file,list);
	}
	var app=ac.getAppInfo(chip.name);
	var g= app.getGraph();
	var inputs=g.enumInputPin();
	for(var i=0;i<inputs.length;i++){
		var input=inputs[i];
		if(ac.isPlugInputServer(chip.name,input,'website')){
			continue;
		}
		//var  args=new InputServerConnectArgs();
		ac.plugInputServer(chip.name,input,	'website', null); 
	}
}