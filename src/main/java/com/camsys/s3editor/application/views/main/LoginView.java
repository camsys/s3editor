package com.camsys.s3editor.application.views.main;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.camsys.s3editor.application.services.RouteVizAuthenticationService;
import com.camsys.s3editor.application.views.main.LoginView;

@Route(value = "login")
@PageTitle("Login")
public class LoginView extends AppLayout {

	public static RouteVizAuthenticationService authenticationService = RouteVizAuthenticationService.getInstance();

    public LoginView() {
        FlexLayout centeringLayout = new FlexLayout();
        centeringLayout.setSizeFull();
        centeringLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centeringLayout.setAlignItems(Alignment.CENTER);
        
        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addLoginListener(this::login);

        centeringLayout.add(loginForm);        
    	setContent(centeringLayout);
    }

    private void login(LoginForm.LoginEvent event) {
    	if(authenticationService.checkUser(event.getUsername(), event.getPassword())) {
            getUI().get().navigate("");
        } else {
            event.getSource().setError(true);
        }
    }
}
