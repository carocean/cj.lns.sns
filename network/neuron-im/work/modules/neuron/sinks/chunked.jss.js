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
var CircuitException = Java.type('cj.studio.ecm.graph.CircuitException');
var String = Java.type('java.lang.String');
var IFeedback=Java.type('cj.studio.ecm.frame.IFeedback');
var ISink=Java.type('cj.studio.ecm.graph.ISink');
var NetConstans=Java.type('cj.studio.ecm.net.nio.NetConstans');
var StringUtil=Java.type('cj.ultimate.util.StringUtil');





function doChunked(chunked,frame,circuit,plug){
	var fb=circuit.feedback(IFeedback.KEY_OUTPUT_FEEDBACK);
	var cmd=chunked.command();
	//print('.....'+chunked);
	if('open'.equals(cmd)){
		//print('-------begin--'+circuit);
		var len=chunked.content().readableBytes();
		var range=chunked.head('Neuron-Chunked-Range');
		if(StringUtil.isEmpty(range)){
			range="8192";
		}
		fb.doBack(chunked,circuit);
		var backcir=new Circuit('http/1.1 200 ok');
		var atts = circuit.enumAttributeName();
		for (var i=0;i<atts.length;i++) {
			var key=atts[i];
			backcir.attribute(key, circuit.attribute(key));
		}
	
		var pos=len;
		var root=frame.rootName();
		var url=String.format('/%s%s',root,chunked.url());
		for(;;){
			chunked=new Frame(String.format('chunked %s chunked/1.0',url));
			chunked.head('Cookie',frame.head('Cookie'));
			chunked.head('Neuron-Chunked-Position',pos+'');
			chunked.head('Neuron-Chunked-Range',range);
			chunked.head(NetConstans.FRAME_HEADKEY_CIRCUIT_SYNC,'true');
			
			plug.flow(chunked,backcir);
			
			
			chunked=new Frame(backcir.content().readFully());
			var readsize=chunked.content().readableBytes();
			//print('...++..'+chunked+' '+frame.head('Cookie')+' '+readsize);
			if('piggyback'.equals(chunked.command())){
				throw new CircuitException('503','读取资源块错误：'+chunked.url());
			}
			if(readsize<1||'close'.equals(chunked.head('command'))){
				//close
				fb.doBack(chunked,backcir);
				//print('============end'+circuit);
				break;
			}else{
				fb.doBack(chunked,circuit);
				pos+=readsize;
			}
			
		}
	}else{
		throw new CircuitException('503','错误的返回块');
	}
	
}

exports.flow = function(frame, circuit, plug) {
	circuit.feedbackSetSource(IFeedback.KEY_INPUT_FEEDBACK);
	circuit.feedback(IFeedback.KEY_INPUT_FEEDBACK).plugSink('back-chunked',new ISink(){
			flow:function(f,c,p){
				if('CHUNKED/1.0'.equals(f.protocol())){
					doChunked(f,frame,circuit,plug);
					circuit.message('ok');
					circuit.status('200');
					circuit.piggybacking(false);
					return;
				}else{
					circuit.status(f.head('status'));
					circuit.message(f.head('message'));
					circuit.contentType('frame/bin');
					circuit.content().writeBytes(f.toBytes());
					
				}
			}
		});
	
	plug.flow(frame,circuit);
	
	
	//	var newcir=new Circuit(back);
	//	circuit.coverFrom(newcir);
		
	
}