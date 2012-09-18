package org.rest.client.ui.desktop;

import java.sql.Date;

import org.rest.client.place.RequestPlace;
import org.rest.client.resources.AppCssResource;
import org.rest.client.resources.AppResources;
import org.rest.client.storage.store.objects.RequestObject;
import org.rest.client.ui.SavedView.Presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class SavedListItemViewImpl extends Composite {

	private static SavedListItemViewImplUiBinder uiBinder = GWT
			.create(SavedListItemViewImplUiBinder.class);

	interface SavedListItemViewImplUiBinder extends
			UiBinder<Widget, SavedListItemViewImpl> {
	}
	
	final private Presenter listener;
	final private RequestObject requestObject;
	AppCssResource appStyle = AppResources.INSTANCE.appCss();
	
	@UiField InlineLabel dateLabel;
	@UiField InlineLabel methodLabel;
	@UiField InlineLabel urlValue;
	@UiField InlineLabel nameLabel;
	@UiField DivElement detailedPanel;
	@UiField HTMLPanel urlLabel;
	@UiField SpanElement encoding;
	@UiField SpanElement payload;
	@UiField SpanElement headers;
	@UiField HTMLPanel container;
	
	public SavedListItemViewImpl(Presenter listener, RequestObject requestObject) {
		this.listener = listener;
		this.requestObject = requestObject;
		initWidget(uiBinder.createAndBindUi(this));
		
		urlLabel.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				e.preventDefault();
				doOnExpand();
			}
		}, ClickEvent.getType());
		
		long time = (long) requestObject.getTime();
		Date date = new Date(time);
		String data = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(date);
		dateLabel.setText(data);
		
		this.nameLabel.setText(requestObject.getName());
		this.methodLabel.setText(requestObject.getMethod());
		this.urlValue.setText(requestObject.getURL());
		
		
		if(requestObject.getPayload() != null){
			payload.setInnerText(requestObject.getPayload());
		}
		if(requestObject.getHeaders() != null){
			headers.setInnerText(requestObject.getHeaders());
		}
		if(requestObject.getEncoding() != null){
			encoding.setInnerText(requestObject.getEncoding());
		}
	}
	
	@UiHandler({"methodLabel"})
	void onExpand(ClickEvent e){
		e.preventDefault();
		doOnExpand();
	}
	@UiHandler("select")
	void onSelectItem(ClickEvent e){
		e.preventDefault();
		listener.goTo(RequestPlace.Tokenizer.fromSaved(requestObject.getId()));
	}
	
	private void doOnExpand(){
		if(detailedPanel.getClassName().contains(appStyle.hidden())){
			expandDetails();
		} else {
			hideDetails();
		}
	}
	void hideDetails(){
		container.removeStyleName(appStyle.historySelected());
		detailedPanel.addClassName(appStyle.hidden());
	}
	void expandDetails(){ 
		container.addStyleName(appStyle.historySelected());
		detailedPanel.removeClassName(appStyle.hidden());
	}
}
