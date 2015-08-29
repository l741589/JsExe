package com.bigzhao.jsexe.engine.net;

import com.bigzhao.jsexe.engine.Engine;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ZElement {
	
	private Element elem;

	public ZElement(Element elem) {
		this.elem=elem;
	}
	
	public String attr(String key){
		return elem.attr(key);
	}

	public String val(){
		return elem.val();
	}
	
	public String text(){
		return elem.text();
	}
	
	public ZElement prev(){
		return new ZElement(elem.previousElementSibling());
	}
	
	public ZElement next(){
		return new ZElement(elem.nextElementSibling());
	}
	
	public String html(){
		return elem.html();
	}
	
	public Object children(){
		return elems2Array(elem.children());
	}
	
	public ZElement parent(){
		return new ZElement(elem.parent());
	}
	
	public String outerHtml(){
		return elem.outerHtml();
	}
	
	public Object ownText(){
        return elem.ownText();
	}
	
	public String data(){
		return elem.data();		
	}
	
	public String[] classes(){
		return elem.classNames().toArray(new String[0]);
	}
	
	public String id(){
		return elem.id();
	}	

	public Object select(String query){
		return elems2Array(elem.select(query));	
	}	
	
	public boolean hasAttr(String attr){
		return elem.hasAttr(attr);
	}
	
	public boolean hasClass(String cls){
		return elem.hasAttr(cls);
	}
	
	public boolean hasText(){
		return elem.hasText();
	}
	
	protected Object elems2Array(Elements elems) {
		Object[] arr=new Object[elems.size()];
		for (int i=0;i<arr.length;++i) arr[i]=new ZElement(elems.get(i));
		return Engine.newArray(arr);
	}
	
	public String name(){
		return elem.tagName();	
	}
	
	public String tag(){
		return elem.tagName();	
	}
	
	@Override
	public String toString() {
		return elem.toString();
	}
}
