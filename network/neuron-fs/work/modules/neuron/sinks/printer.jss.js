/*
 * 说明：
 * 作者：extends可以实现一种类型，此类型将可在java中通过调用服务提供器的.getServices(type)获取到。
 * <![jss:{
		scope:'runtime',
		extends:'cj.studio.ecm.graph.ISink'
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
var Circuit = Java.type('cj.studio.ecm.frame.Circuit');
var String = Java.type('java.lang.String');


exports.flow = function(frame, circuit, plug) {
	print('-----------'+frame)
	var heads=frame.enumHeadName();
	print('head:')
	for(var i=0;i<heads.length;i++){
		print(String.format('\t%s:%s',heads[i],frame.head(heads[i])	));
	}
	var params=frame.enumParameterName();
	print('parameter:')
	for(var i=0;i<params.length;i++){
		print(String.format('\t%s:%s',params[i],frame.parameter(params[i])	));
	}
	print('-----------end');
	print('');
	plug.flow(frame,circuit);
}

