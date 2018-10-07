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
var HashMap = Java.type('java.util.HashMap');
var Destination = Java.type('cj.lns.chip.sns.neuron.core.Destination');

exports.broadcast = function(frame, circuit,
		 clients,builder) {
	var customer=frame.head('csc-customer');
	//var visitor=frame.head('csc-visitor');

	var c=clients.get(customer);
	if(c!=null){
		//print('发现活动目标：'+customer);
		c.buildNetGraph().netInput().flow(frame, circuit);
		return;
	}
	
	var dest=builder.getValidDestination(customer);
	if(dest==null){
		//print('发现新目标：'+customer);
		var protocol=customer.substring(0,customer.indexOf('://'));
		var address=customer.substring(protocol.length()+3,customer.length());
		var arr=address.split(':');
		dest=new Destination();
		dest.setName(customer);
		dest.setInetHost(arr[0]);
		dest.setPort(arr[1]);
		dest.setUseShared(true);
		if('http'==protocol){
			protocol='rio-http';
		}
		dest.setProtocol(protocol);
		var props=new HashMap();
		props.put('workThreadCount','4');
		dest.setProps(props);
		builder.buildAddDestination(dest);
	}else{
		//print('发现预定义的有效目标：'+customer);
		builder.build(dest);
	}
	
	
	
	c = clients.get(customer);
	//print('++++++'+c);
	c.buildNetGraph().netInput().flow(frame, circuit);
	
}

