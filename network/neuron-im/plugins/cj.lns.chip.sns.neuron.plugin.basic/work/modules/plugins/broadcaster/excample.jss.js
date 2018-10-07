/*
 * 说明：本示例适用于多播信道
 * 作者：
 * extends可以实现一种类型，此类型将可在java中通过调用服务提供器的.getServices(type)获取到。
 * <![jss:{
		scope:'runtime',
		extends:'cj.lns.chip.sns.neuron.IBroadcaster'
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


exports.broadcast = function(frame, circuit,
		 clients,builder) {
	
	var activedDestCol=builder.getActivedDestinations();
	if(!activedDestCol.isEmpty()){
		var it=activedDestCol.keySet().iterator();
		while(it.hasNext()){
			var name=it.next();
			var c=clients.get(name);
			print('使用现有活动目标：'+name);
			c.buildNetGraph().netInput().flow(frame.copy(), circuit);
			break;
		}
		return;
	}
	
	var names=builder.enumValidDestination();
	print('选取了新目标：'+names[0]);
	
	var dest=builder.getValidDestination(names[0]);
	builder.build(dest);
	var c = clients.get(names[0]);
	c.buildNetGraph().netInput().flow(frame, circuit);
	
}

