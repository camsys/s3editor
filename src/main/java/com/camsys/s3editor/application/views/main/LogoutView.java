package com.camsys.s3editor.application.views.main;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.camsys.s3editor.application.services.RouteVizAuthenticationService;
import com.camsys.s3editor.application.views.main.LogoutView;

@Route(value = "logout")
@PageTitle("Logout")
public class LogoutView extends AppLayout {

	public static RouteVizAuthenticationService authenticationService = RouteVizAuthenticationService.getInstance();

    public LogoutView() {
    	authenticationService.logout();

    	getUI().get().navigate("login");
    }
}
