package com.camsys.s3editor.application;

import com.camsys.s3editor.application.services.RouteVizAuthenticationService;
import com.camsys.s3editor.application.views.main.LoginView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class RequiresAuthenticationInitListener implements VaadinServiceInitListener {

	public static RouteVizAuthenticationService authenticationService = RouteVizAuthenticationService.getInstance();

	@Override
    public void serviceInit(ServiceInitEvent initEvent) {
        initEvent.getSource().addUIInitListener(uiInitEvent -> {
            uiInitEvent.getUI().addBeforeEnterListener(enterEvent -> {
                if (authenticationService.getCurrentUser() == null 
                		&& ! LoginView.class.equals(enterEvent.getNavigationTarget()))
                    enterEvent.rerouteTo(LoginView.class);
            });
        });
    }
}